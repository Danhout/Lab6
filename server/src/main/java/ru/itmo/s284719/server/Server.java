package ru.itmo.s284719.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.itmo.s284719.network.Converters;
import ru.itmo.s284719.network.Parser;
import ru.itmo.s284719.network.SpaceMarine;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Server's main class.
 *
 * @version 0.3
 * @author Danhout.
 */
public class Server {
    /**
     * The default server's port.
     */
    public static final int DEFAULT_PORT = 8000;

    /**
     * Main function for server.
     *
     * @param args the file's name with collection in format JSON.
     */
    public static void main(String[] args) {
        // create GSON's parser for format JSON.
        final Gson gson = Converters.registerZoneId(new GsonBuilder()).setPrettyPrinting().create();

        // initialization system's streams of server with auto-flush.
        PrintWriter err = new PrintWriter(
                new OutputStreamWriter(
                        System.err, Charset.forName("UTF-8")), true);
        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        System.out, Charset.forName("UTF-8")), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        System.in, Charset.forName("UTF-8")));

        // read collection from file to program.
        // declare file's name.
        String fileName;

        try {
            // initialization file's name.
            fileName = args[0];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            // if (program's arguments are empty)
            // than: print about the possibility.
            err.println("The source file path wasn't passed.");
            err.println("You must pass the path as a command line argument");
            // and initialise file's name is empty.
            fileName = "base.json";
        }

        // create my parser.
        //Parser parser = new Parser();

        // declare server's collection (PriorityQueue<SpaceMarine>).
        PriorityQueue<SpaceMarine> queue = null;

        // parse file's text in format JSON to the collection.
        try {
            // if (file's name isn't empty) than: parse that.
            if (fileName != "") {
                List<String> strings = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
                StringBuilder jsonStrBuild = new StringBuilder();
                for (String str : strings) jsonStrBuild.append(str).append("\r\n");
//                Type type = new TypeToken<ArrayList<SpaceMarine>>(){}.getType();
                Type type = new TypeToken<PriorityQueue<SpaceMarine>>(){}.getType();

//                ArrayList<SpaceMarine> list = gson.fromJson(jsonStrBuild.toString(), new ArrayList<SpaceMarine>().getClass());
//                out.println(new SpaceMarine());
//                for (Object object : list) { out.println(object); }
                queue = gson.fromJson(jsonStrBuild.toString(), type);
                // print about successful reading of the file.
                out.println("\u001B[32m" + "File with name: \"" + fileName + "\" received" + "\u001B[32m");
            }
        } catch (FileNotFoundException e) {
            // if (file isn't found) than: print the exception.
            err.println("File with name: \"" + fileName + "\" not found\r\n" +
                    "Must pass the path of the source file as a command line argument");
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            // else if (file's text isn't in reading format) than: print the exception.
            err.println("Invalid format of the source file with the name: \"" + fileName + "\"\r\n" +
                    "Must pass the path of the source file as a command line argument");
        } catch (IOException e) {
            // else if (exception in input or output) than: print the exception.
            err.println("Input/Output with the file ended abruptly\r\n" +
                    "Must pass the path of the source file as a command line argument");
        }

        // if (collection wasn't create) than: create new empty collection.
        if (queue == null) {
            queue = new PriorityQueue<SpaceMarine>();
            // print about that.
            out.println("\u001B[32m" + "Empty collection is created" + "\u001B[0m");
        } else {
            // else: print about successful creation the collection.
            out.println("\u001B[32m" + "Collection is created" + "\u001B[0m");
        }

        // declaration server's PORT.
        int port;

        // initialization PORT for the server and start working of the server.
        try {
            // print info for input server's PORT.
            out.println("Server's PORT:");
            out.print("$");
            out.flush();

            // if (received end symbol) than: print the information and exit the program.
            in.mark(1);
            if (in.read() == -1) {
                err.println("Received the program end symbol.");
                System.exit(0);
            }
            in.reset();
            // else: read line from the console and normalise that.
            String str = Parser.normalise(in.readLine());
            // if (line is empty) than: throw NullPointerException.
            if (str == null || str.equals("")) { throw new NullPointerException(); }
            port = Integer.parseInt(str);
            // else if (the port incorrect) than: throw new IllegalAgrumentException.
            if (port < 1024 || port > 65535) throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            // print default port.
            err.println("Server's default PORT: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } catch (IllegalArgumentException e) {
            // print exception and default port.
            err.println("Incorrect PORT");
            err.println("Server's default PORT: " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        } catch (IOException e) {
            // if (received IOException) than: print that and exit from the program.
            err.println(e.getMessage());
            return;
        }

        // create server.
        new ServerConsole(port, queue);
    }
}
