package ru.itmo.s284719.client;
import ru.itmo.s284719.network.*;
import ru.itmo.s284719.network.commands.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Client's class with really commands.
 *
 * @version 0.4
 * @author Danhout.
 */
public class ClientConsole extends ModifiedCMD {
    /**
     * The server's IP.
     */
    private final String IP;
    /**
     * The server's PORT.
     */
    private final int PORT;
    /**
     * The client's channel.
     */
    private SocketChannel channel;
    /**
     * The field with information about connection.
     */
    public boolean wasConnected = false;

    /**
     * Client's constructor.
     *
     * @param address the server's IP.
     * @param port the server's PORT.
     */
    public ClientConsole(String address, int port) throws UnknownHostException {
        // initialise server's IP and PORT.
        IP = InetAddress.getByName(address).getHostAddress();
        PORT = port;

        // connect to server.
        connectionToServer(IP, PORT);
    }

    private void connectionToServer(String ip, int port) {
        do {
            try {
                channel = SocketChannel.open(new InetSocketAddress(IP, PORT));
                channel.configureBlocking(false);
                if (channel == null) {
                    throw new IOException("The server is disconnected.");
                }
                out.println("\u001B[32m" + "Connected to server with IP: " + IP + ", Port: " + PORT + "..." + "\u001B[0m");
                wasConnected = true;
                do {
                    // execute command from the server's console.
                    runCommand(readConsoleLine());
                } while (true);
            } catch (IOException e) {
                // if (creating is fail) than: create new client.
                if (wasConnected) {
                    err.println("Server with IP: " + IP + ", Port: " + PORT + " is unavailable.");
                }
                err.print("Attempt to connect to the server   ");
                do {
                    try {
                        Thread.sleep(500);
                        err.print("\b\b\b");
                        err.flush();
                        Thread.sleep(500);
                        err.print(".");
                        err.flush();
                        Thread.sleep(500);
                        err.print(".");
                        err.flush();
                        Thread.sleep(500);
                        err.print(".");
                        err.flush();
                        channel = SocketChannel.open(new InetSocketAddress(IP, PORT));
                        channel.configureBlocking(false);

                    } catch (InterruptedException | IOException e2) {
//                          // for logging.
//                          e2.printStacktrace();
                    }
                } while (channel == null || !channel.isConnected());
                err.println();
            }
        } while (true);
    }


    public void checkConnection() throws IOException {
        if (!channel.isConnected()) {
            throw new IOException("Server with IP: " + IP + ", Port: " + PORT + " is unavailable.");
        } else if (!wasConnected) {
            out.println("\n\u001B[32m" + "Connected to server with IP: " + IP + ", Port: " + PORT + "..." + "\u001B[0m");
        } else { wasConnected = true; }
    }

    /**
     * Output information about the collection to the standard output stream
     * (type, initialization date, number of elements, etc).
     *
     * @param args arguments for the command.
     */
    @Override
    public void info(String[] args) throws IOException {
        // if (the command have easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("info: this command hasn't parameters.");
            return;
        }

        // else print to client's console an information about the collection.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new Info());
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
            } else {
                out.println(object);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Output to standard output stream all
     * (the elements of the collection in the string representation).
     *
     * @param args the command's arguments.
     */
    @Override
    public void show(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("show: this command hasn't parameters.");
            return;
        }

        // else: print that.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new Show());
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
                return;
            }

            out.println(object);
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Add a new element to the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void add(String[] args) throws IOException {
        // if (the command has easy parameters) than: throw exception and return.
        if (args == null || args.length != 0) {
            err.println("add: this command has one composite parameter {element}.");
            return;
        }
        // else: read composite parameter (SpaceMarine) and add that to the collection.
        try {
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();

            checkConnection();
            ObjectSocketChannel.sendObject(channel, new Add(spaceMarine));
            Object object = ObjectSocketChannel.getObject(channel);


            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Update the value of a collection element whose ID is equal to the specified one.
     *
     * @param args the command's arguments.
     */
    @Override
    public void update(String[] args) throws IOException {
        // declare element's ID.
        int id = 0;

        // if (the command hasn't only one easy parameter) than: print exception and return.
        if (args == null || args.length != 1) {
            err.println("update: this command has one easy \"ID\" and one composite {element} parameters.");
            return;
        }

        // else: check easy parameter (Integer, not null, greater than zero) and read composite parameter (SpaceMarine).
        try {
            id = Integer.parseInt(args[0]);
            if (id <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            // if (check is fail) than: print exception and return.
            err.println("update: this command has one easy \"ID\" and one composite {element} parameters.");
            return;
        }

        try {
            // read composite parameter (SpaceMarine).
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();
            // update element from collection with the ID.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new Update(id, spaceMarine));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Delete an item from the collection by its ID.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeById(String[] args) throws IOException {
        try {
            // if (the command hasn't only one easy parameter) than: print exception and return.
            if (args == null || args.length != 1) {
                err.println("remove_by_id: this command has one easy parameter \"ID\".");
                return;
            }

            // else: check parameter (Integer, not null, greater than zero)
            // and remove element from collection with the ID.
            int id = Integer.parseInt(args[0]);
            if (id <= 0) {
                throw new IllegalArgumentException();
            }

            checkConnection();
            ObjectSocketChannel.sendObject(channel, new RemoveById(id));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        } catch (IllegalArgumentException e) {
            // if (check is fail) than: print exception and return.
            err.println("remove_by_id: the command's parameter is a positive number.");
        }
    }

    /**
     * Clear the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void clear(String[] args) throws IOException {
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new Clear());
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Save the collection to a file.
     *
     * @param args the command's arguments.
     */
    @Override
    public void save(String[] args) {
        err.println("save: this command is not available on the client application.");
    }

    /**
     * Terminate the program without saving in to a fail.
     *
     * @param args the command's arguments.
     */
    @Override
    public void exit(String[] args) throws IOException {
        if (args == null || args.length != 0) {
            err.println("exit: this command hasn't parameters.");
            return;
        }

        try {
            channel.close();
        } finally {
            System.exit(0);
        }
    }

    /**
     * Output the first item in the collection and deletes it.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeHead(String[] args) throws IOException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_head: this command hasn't parameters.");
            return;
        }

        // else if (the queue isn't empty): print and remove element from head of the collection.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new RemoveHead());
            Object object = ObjectSocketChannel.getObject(channel);

            if (object == null) {
            } else if (object.getClass().equals(SpaceMarine.class)) {
                out.println(object);
            } else {
                err.println("Invalid object type returned: expected \"Optional<SpaceMarine>\".");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new item to the collection if its value is less than the smallest item in this collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void addIfMin(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("add_if_min: this command has one composite parameter {element}.");
            return;
        }

        try {
            // else: read composite parameter (SpaceMarine).
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();
            // if (the element is minimal) than: add the element to the collection.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new AddIfMin(spaceMarine));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Remove all items from the collection that exceed the specified value.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeGreater(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_greater: this command has one composite parameter {element}.");
            return;
        }

        try {
            // read composite parameter (SpaceMarine).
            checkConnection();
            SpaceMarine spaceMarine = inputSpaceMarine();
            // remove all elements from the collection greater than the element.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new RemoveGreater(spaceMarine));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Delete a single item from the collection,
     * the value of the field whose height is equivalent to the specified one.
     *
     * @param args the command's arguments.
     */
    @Override
    public void removeAnyByHeight(String[] args) throws IOException {
        // if (the command's parameters greater than one) than: print exception and return.
        if (args == null || args.length > 1) {
            err.println("remove_aby_by_height: this command has one easy \"Height\" or hasn't parameter.");
            return;
        }

        // else if (the command hasn't parameters) than: the command has one easy parameter "zero".
        if (args.length == 0) {
            checkConnection();
            ObjectSocketChannel.sendObject(channel,new RemoveAnyByHeight(0));
            return;
        }

        // check the parameter (Integer)
        // and remove any element from the collection whose has a height equal to the height.
        try {
            int height = Integer.parseInt(args[0]);

            checkConnection();
            ObjectSocketChannel.sendObject(channel, new RemoveAnyByHeight(height));
            Object object = ObjectSocketChannel.getObject(channel);

            if (object != null) {
                err.println("Invalid object type returned: expected \"null\".");
            }
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        } catch (NumberFormatException e) {
            // if (check is fail) than: print the exception and return.
            err.println("remove_any_by_height: the command's parameter is a number.");
        }
    }

    /**
     * Output the average value of the height field for all items in the collection.
     *
     * @param args the command's arguments.
     */
    @Override
    public void averageOfHeight(String[] args) throws IOException {
        // if (the command has parameters) than: print the exception and return.
        if (args == null || args.length != 0) {
            err.println("average_of_height: this command hasn't parameters.");
            return;
        }

        // else: get and print server's answer.
        try {
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new AverageOfHeight());
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
                return;
            }

            String str = (String) object;
            out.println(str);
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Output the number of elements
     * whose melee Weapon field value is greater than the specified one.
     *
     * @param args the command's arguments.
     */
    public void countGreaterThanMeleeWeapon(String[] args) throws IOException {
        // if (the command hasn't only one easy parameter) than: print the exception and return.
        if (args == null || args.length != 1) {
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\".");
            return;
        }

        // else: check parameter(MeleeWeapon) and print count elements from the collection
        // with MeleeWeapon greater than the MeleeWeapon.
        try {
            MeleeWeapon meleeWeapon = MeleeWeapon.valueOf(args[0]);
            // if (check is fail) than: print the exception and return.
            if (meleeWeapon == null) {
                err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\" type of enumeration.");
                return;
            }

            // else: print the count.
            checkConnection();
            ObjectSocketChannel.sendObject(channel, new CountGreaterThanMeleeWeapon(meleeWeapon));
            Object object = ObjectSocketChannel.getObject(channel);

            if (!object.getClass().equals(String.class)) {
                err.println("Invalid object type returned: expected \"String\".");
                return;
            }

            out.println(object);
        } catch (ClassNotFoundException e) {
            err.println(Arrays.toString(e.getStackTrace()));
        } catch (IllegalArgumentException e) {
            // warning: the code should not start.
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\" type of enumeration.");
        }
    }
}