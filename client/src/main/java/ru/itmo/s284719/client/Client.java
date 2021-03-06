package ru.itmo.s284719.client;

import ru.itmo.s284719.network.Parser;

import java.io.*;
import java.nio.charset.Charset;

/**@version 0.2
 * @author Danhout
 * Client's main class*/
public class Client {
    /**
     * Default server's IP.
     */
    public static final String DEFAULT_IP = "127.0.0.1";
    /**
     * Default server's PORT.
     */
    public static final int DEFAULT_PORT = 8000;

    /**
     * Main client's function.
     */
    public static void main(String[] args) throws IOException {

        // create buffered system's streams with auto-flush.
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        System.in, Charset.forName("UTF-8")));
        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        System.out, Charset.forName("UTF-8")), true);
        PrintWriter err = new PrintWriter(
                new OutputStreamWriter(
                        System.err, Charset.forName("UTF-8")), true);

        // declare client.
        ClientConsole clientConsole;

        // create server's IP and PORT for connection.
        String ip = DEFAULT_IP;
        int port = DEFAULT_PORT;

        // read server's IP.
        try {
            // print info for input server's IP.
            out.println("Server's IP:");
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
            ip = Parser.normalise(in.readLine());
            // if (line is empty) than: throw NullPointerException.
            if (ip.equals("")) { throw new NullPointerException(); }
            if (!ip.equals("localhost")) {
                String[] digitsIP = ip.split("\\.");
                if (digitsIP.length != 4) throw new IllegalArgumentException();
                for (int i = 0; i < 4; ++i) {
                    int digit = Integer.parseInt(digitsIP[i]);
                    if (digit < 0 || digit > 255) throw new IllegalArgumentException();
                }
            }
        } catch (NullPointerException e) {
            // print default IP.
            err.println("Server's default IP: " + DEFAULT_IP + ".");
            ip = DEFAULT_IP;
        } catch (IllegalArgumentException e) {
            // print exception and default IP.
            err.println("Incorrect IP.");
            err.println("Server's default IP: " + DEFAULT_IP + ".");
            ip = DEFAULT_IP;
        }

        // read server's PORT.
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
            if (str.equals("")) { throw new NullPointerException(); }
            port = Integer.parseInt(str);
            // else if (the PORT incorrect) than: throw new IllegalAgrumentException.
            if (port < 1024 || port > 65535) throw new IllegalArgumentException();
        } catch (NullPointerException e) {
            // print default PORT.
            err.println("Server's default PORT: " + DEFAULT_PORT + ".");
            port = DEFAULT_PORT;
        } catch (IllegalArgumentException e) {
            // print exception and default PORT.
            err.println("Incorrect PORT.");
            err.println("Server's default PORT: " + DEFAULT_PORT + ".");
            port = DEFAULT_PORT;
        }

        // create client.
        new ClientConsole(ip, port);
    }
}
