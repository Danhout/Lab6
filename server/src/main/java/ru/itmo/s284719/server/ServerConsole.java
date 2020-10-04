package ru.itmo.s284719.server;

import ru.itmo.s284719.network.*;
import ru.itmo.s284719.network.commands.Command;
import ru.itmo.s284719.network.commands.*;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Server's class for commands.
 *
 * @version 0.5
 * @author Danhout
 */
public class ServerConsole extends ModifiedCMD {
    /**
     * The computer's local IP.
     */
    private String IP = "localhost";
    /**
     * The server's working port.
     * It's default value of 8000.
     */
    private int PORT = Server.DEFAULT_PORT;
    /**
     * The server's channel.
     */
    private ServerSocketChannel serverChannel;
    /**
     * The selector for registering server's channel for operation accept.
     */
    private Selector serverSelector;
    /**
     * The selector for registering client's channel for operation read.
     */
    private Selector clientSelector;
    /**
     * The list with client's channels.
     */
    private List<SocketChannel> listChannels = new LinkedList<>();
    /**
     * The thread for processing connections.
     */
    private Thread threadConnections;
    /**
     * The field with information about working server.
     */
    private boolean wasWorking = false;


    /**
     * Constructor without parameters (with default parameters).
     */
    public ServerConsole() {
        this(Server.DEFAULT_PORT, new PriorityQueue<SpaceMarine>());
    }
    /**
     * Constructor with all parameters.
     *
     * @param port the server's port.
     * @param queue the queue for the server.
     */
    public ServerConsole(int port, PriorityQueue<SpaceMarine> queue) {
        // create ServerConsole with the SpaceMarine's queue and default functions.
        super(queue);

        try {
            // initialization the IP and the port.
            IP = InetAddress.getByName("localhost").getHostAddress();
            PORT = port;

            // open server's and client's selectors.
            serverSelector = Selector.open();
            clientSelector = Selector.open();

            // open server's channel.
            serverChannel = ServerSocketChannel.open();
            // initialization not-blocking server's channel and register the channel to the server's selector
            // for operation accept of server's channel.
            serverChannel.bind(new InetSocketAddress(PORT)).configureBlocking(false).register(serverSelector, SelectionKey.OP_ACCEPT);

            // print IP and port about the created server.
            out.println("Server with IP: " + IP + ", Port: " + PORT + " is working...");

            // was print about working the server.
            wasWorking = true;

            // initialization new thread for processing of client-server connections.
            threadConnections = new Thread(() -> {
                do {
                    try {
                        // select count new connections now.
                        int serverSelect = serverSelector.selectNow();
                        // if (the count more than zero) than: received new connections.
                        if (serverSelect != 0) {
                            Iterator<SelectionKey> keysServer = serverSelector.selectedKeys().iterator();
                            while (keysServer.hasNext()) {
                                try {
                                    // received server-client channel and register that to clientSelector for operation read
                                    SocketChannel channel = ((ServerSocketChannel) keysServer.next().channel()).accept();
                                    channel.configureBlocking(false).register(clientSelector, SelectionKey.OP_READ);
                                    // add the channel to listChannels with client's channels.
                                    listChannels.add(channel);
//                                // for logging.
//                                // print information about the new connection.
//                                out.println("Client with IP: " + channel.socket().getInetAddress().getHostAddress()
//                                        + ", PORT: " + channel.socket().getPort()
//                                        + " is connected");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    keysServer.remove();
                                }
                            }
                        }

                        // select count new client's commands.
                        int clientSelect = clientSelector.selectNow();
                        // if (the count more than zero) than: execute the commands.
                        if (clientSelect != 0) {
                            Iterator<SelectionKey> keysClient = clientSelector.selectedKeys().iterator();
                            while (keysClient.hasNext()) {
                                // get client's channel.
                                SocketChannel channel = (SocketChannel) keysClient.next().channel();
                                try {
                                    // execute client's command.
                                    runClientCommand(channel);
                                } catch (Exception e) {
                                    channel.close();
                                } finally {
                                    keysClient.remove();
                                }
                            }
                        }
                    } catch (IOException e) {
                        // for logging.
                        // warning: the code should not start.
                        e.printStackTrace();
                    }

                    // remove closed client's channels from client's selector and list with channels.
                    Iterator<SocketChannel> channels = listChannels.iterator();
                    while (channels.hasNext()) {
                        SocketChannel channel = channels.next();
                        if (!channel.isConnected()) {
                            // for logging.
//                                out.println("Client with IP: " + channel.socket().getInetAddress().getHostAddress()
//                                        + ", PORT: " + channel.socket().getPort()
//                                        + " is disconnected");
                            clientSelector.keys().remove(channel.keyFor(clientSelector));
                            channels.remove();
                        }
                    }
                } while (true);
            });
            // start the thread for processing connections.
            threadConnections.start();

            // work server's console.
            do {
                try {
                    // execute command from the server's console.
                    runCommand(readConsoleLine());
                } catch (Exception e) {
                    // if (received exception) than: execute command exit.
                    runCommand("exit");
                }
            } while (true);
        } catch (IOException e) {
            err.println(e.getMessage());
            if (wasWorking) { err.println("The server is closed."); }
        }
    }

    /**
     * Run client's command.
     *
     * @param channel the client's channel.
     */
    private void runClientCommand(SocketChannel channel) throws IOException, ClassNotFoundException {
        Object object = ObjectSocketChannel.getObject(channel);
        Command command = (Command) object;

        // see command's name.
        switch (command.getName()) {
            // add spaceMarine to queue.
            case "add":
                SpaceMarine spaceMarine = ((Add) object).spaceMarine;
                queue.add(spaceMarine);
                ObjectSocketChannel.sendObject(channel, null);
                break;

            // if (the the spaceMarine's less than all spaceMarines from the queue)
            // than: add that to queue
            case "add_if_min":
                // get minimal spaceMarine from the queue.
                spaceMarine = ((AddIfMin) object).spaceMarine;
                Optional<SpaceMarine> optional = queue.stream().min(SpaceMarine::compareTo);
                // if (queue isn't empty and new spaceMarine less than the old minimal spaceMarine)
                if (!optional.isPresent() || spaceMarine.compareTo(optional.get()) < 0) {
                    // than: add new spaceMarine to the queue.
                    queue.add(spaceMarine);
                }
                ObjectSocketChannel.sendObject(channel, null);
                break;

            // send average of height of spaceMarines from the queue to client.
            case "average_of_height":
                String str;
                if (queue.isEmpty()) {
                    str = "The average value of the height: 0.";
                } else {
                    int result = queue.stream().mapToInt(SpaceMarine::getHeight).sum();
                    str = "The average value of the height: " + ((double) result) / queue.size() + ".";
                }
                ObjectSocketChannel.sendObject(channel, str);
                break;

            // clear the queue.
            case "clear":
                queue.clear();
                ObjectSocketChannel.sendObject(channel, null);
                break;

            // send count spaceMarines from the queue greater than the meleeWeapon.
            case "count_greater_than_melee_weapon":
                MeleeWeapon meleeWeapon = ((CountGreaterThanMeleeWeapon) object).meleeWeapon;
                long count = queue.stream()
                        .filter(sM -> sM.getMeleeWeapon().compareTo(meleeWeapon) > 0)
                        .count();
                str = count + " queue's elements have the value \"Melee Weapon\", greater than the given value.";
                ObjectSocketChannel.sendObject(channel, str);
                break;

            // send info about the queue to client.
            case "info":
                str = "collectionType: PriorityQueue<SpaceMarine>, " +
                        "createTime: " + new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").format(creationTime) +
                        ", length: " + queue.size() + ".";
                ObjectSocketChannel.sendObject(channel, str);
                break;

            // remove any spaceMarine from the queue with height less than the height.
            case "remove_any_by_height":
                int height = ((RemoveAnyByHeight) object).height;
                Iterator<SpaceMarine> iter = queue.iterator();
                while (iter.hasNext()) {
                    SpaceMarine sM = iter.next();
                    if (sM.getHeight().equals(height)) {
                        iter.remove();
                        break;
                    }
                }
                ObjectSocketChannel.sendObject(channel, null);
                break;

            // remove spaceMarine by the ID.
            case "remove_by_id":
                int id = ((RemoveById) object).id;
                iter = queue.iterator();
                while (iter.hasNext()) {
                    SpaceMarine sM = iter.next();
                    if (sM.getId() == id) {
                        iter.remove();
                        break;
                    }
                }
                ObjectSocketChannel.sendObject(channel, null);
                break;

            // remove all spaceMarine from the queue greater than the spaceMarine.
            case "remove_greater":
                spaceMarine = ((RemoveGreater) object).spaceMarine;
                queue.removeIf(sM -> sM.compareTo(spaceMarine) > 0);
                ObjectSocketChannel.sendObject(channel, null);
                break;

            // send and remove spaceMarine from a head of the queue.
            case "remove_head":
                ObjectSocketChannel.sendObject(channel, queue.poll());
                break;

            // send the queue in the format JSON to client.
            case "show":
                List<SpaceMarine> list = queue.stream()
                        .sorted(Comparator.comparing(SpaceMarine::getHeight))
                        .collect(Collectors.toList());
                ObjectSocketChannel.sendObject(channel, gson.toJson(list));
                break;

            // update spaceMarine with same ID.
            case "update":
                Update updateValue = (Update) object;
                id = updateValue.id;
                spaceMarine = updateValue.newSpaceMarine;
                iter = queue.iterator();
                while (iter.hasNext()) {
                    SpaceMarine sM = iter.next();
                    if (sM.getId() == id) {
                        iter.remove();
                        break;
                    }
                }
                queue.add(spaceMarine);
                ObjectSocketChannel.sendObject(channel, null);
                break;

            //if (the command's not found) than: throw exception.
            default:
                throw new IllegalArgumentException("Not found the command: " + command);
        }
    }

    /**
     * Output the first item in the collection and deletes it.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void exit(String[] args) throws IOException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("exit: this command hasn't parameters.");
            return;
        }

        super.save(new String[0]);
        System.exit(0);
    }
}
