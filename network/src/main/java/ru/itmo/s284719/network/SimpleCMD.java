package ru.itmo.s284719.network;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;


/**
 * Class for initialisation map of commands and running they.
 *
 * @version 0.4
 * @author Danhout.
 */
public abstract class SimpleCMD implements Commands {
    /**
     * The map for commands.
     */
    protected final Map<String, Method> mapCommands;
    /**
     * The stack for buffered input's streams.
     */
    public Stack<BufferedReader> stackReaders = new Stack<>();
    /**
     * The set for saving file's names whose run for execution scripts.
     */
    protected Set<String> set = new HashSet<>();
    /**
     * The buffered system's stream of input.
     */
    protected BufferedReader in = new BufferedReader(
            new InputStreamReader(
                    System.in, Charset.forName("UTF-8")));
    /**
     * The buffered system's stream of output with auto-flush.
     */
    protected PrintWriter out = new PrintWriter(
            new OutputStreamWriter(
                    System.out, Charset.forName("UTF-8")), true);
    /**
     * The buffered system's stream of error with auto-flush.
     */
    protected PrintWriter err = new PrintWriter(
            new OutputStreamWriter(
                    System.err, Charset.forName("UTF-8")), true);

    /** Constructor without parameters*/
    public SimpleCMD() {
        mapCommands = new HashMap<>();

        for (Method method : Commands.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command cmd = method.getAnnotation(Command.class);
                mapCommands.put(cmd.name(), method);
            }
        }
    }

    /**
     * Execute command of server's console.
     *
     * @param strLine the normalise line from server's console for running command.
     */
    public void runCommand(String strLine) throws IOException {
        try {
            // separation parameters and command from the line.
            String[] strs = strLine.split(" ");
            String command = strs[0];
            String[] args = Arrays.copyOfRange(strs, 1, strs.length);

            // check correction of the command.
            Method method = mapCommands.get(command);
            if (method == null) {
                // if (command isn't correction) than: print the exception.
                if (!command.equals("")) {
                    err.println("\"" + command + "\" isn't a command.");
                    err.println("help - a command that output the list with open commands.");
                }
                return;
            }
            // else: execute the command.
            ru.itmo.s284719.network.Command cmd = method.getAnnotation(ru.itmo.s284719.network.Command.class);
            method.invoke(this, (Object) args);
        } catch (ArrayIndexOutOfBoundsException e) {
            // warning: the code must not be started.
            err.println("This command wasn't detected.");
            err.println("help - a command that output the list with open commands.");
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // warning: print about thees exceptions.
            err.println("Exception: " + e.getMessage());
            err.println("help - a command that output the list with open commands.");
        } catch (InvocationTargetException e) {
            throw new IOException(e);
        }
    }
}
