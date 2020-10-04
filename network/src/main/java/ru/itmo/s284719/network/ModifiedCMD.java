package ru.itmo.s284719.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Realization really commands.
 *
 * @version 0.4
 * @author Danhout.
 */
public class ModifiedCMD extends SimpleCMD {
    /**
     * The queue for processing space marines and saving they.
     */
    protected final Queue<SpaceMarine> queue;
    /**
     * The time of creation the collection in milliseconds.
     */
    protected final long creationTime;
    /**
     * The universal parser from GSON for format JSON.
     */
    protected final Gson gson = Converters.registerZoneId(new GsonBuilder()).setPrettyPrinting().create();


    /**
     * Constructor without parameters.
     */
    public ModifiedCMD() {
        this(new PriorityQueue<SpaceMarine>());
    }

    /**
     * Constructor that accepts a priority queue of SpaceMarine elements as input.
     * There are initialization the queue and the creation time of the queue.
     */
    public ModifiedCMD(PriorityQueue<SpaceMarine> queue) {
        super();
        this.queue = queue;
        creationTime = System.currentTimeMillis();
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

        // else print to server's console an information about the collection.
        StringBuilder strB = new StringBuilder("collectionType: PriorityQueue<SpaceMarine>, " +
                "createTime: " + new SimpleDateFormat("hh:mm:ss dd-MM-yyyy").format(creationTime) +
                ", length: " + queue.size() + ".");
        out.println(strB.toString());
    }

    /**
     * Output to standard output's stream the collection in format JSON.
     *
     * @param args arguments for the command.
     */
    @Override
    public void show(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("show: this command hasn't parameters.");
            return;
        }

        // else: print that.
        out.println(gson.toJson(queue));
    }

    /**
     * Add a new element to the collection.
     *
     * @param args arguments for the command.
     */
    @Override
    public void add(String[] args) throws IOException {
        // if (the command has easy parameters) than: throw exception and return.
        if (args == null || args.length != 0) {
            err.println("add: this command has one composite parameter {element}.");
            return;
        }
        // else: read composite parameter (SpaceMarine) and add that to the collection.
        queue.add(inputSpaceMarine());
    }

    /**
     * Update the value of a collection element whose ID is equal to the specified one.
     *
     * @param args arguments for the command.
     */
    @Override
    public void update(String[] args) throws IOException {
        // declare element's ID.
        int id;

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

        // read composite parameter (SpaceMarine).
        SpaceMarine spaceMarine = inputSpaceMarine();
        // update element from collection with the ID.
        Iterator<SpaceMarine> iter = queue.iterator();
        while (iter.hasNext()) {
            SpaceMarine sM = iter.next();
            if (sM.getId() == id) {
                iter.remove();
                queue.add(spaceMarine);
                break;
            }
        }
    }

    /**
     * Delete an item from the collection by its ID.
     *
     * @param args arguments for the command.
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
            Iterator<SpaceMarine> iter = queue.iterator();
            while (iter.hasNext()) {
                SpaceMarine sM = iter.next();
                if (sM.getId() == id) {
                    iter.remove();
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            // if (check is fail) than: print exception and return.
            err.println("remove_by_id: the command's parameter is a positive number.");
        }
    }

    /**
     * Clear the collection.
     *
     * @param args arguments for the command.
     */
    @Override
    public void clear(String[] args) throws IOException {
        queue.clear();
    }

    /**
     * Method that saves the collection to the file with name "output.json".
     *
     * @param args arguments for the command.
     */
    @Override
    public void save(String[] args) {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("save: this command hasn't parameters.");
            return;
        }

        // else: save the collection in format JSON to file "base.json".
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream("base.json");
            fout.write(gson.toJson(queue).getBytes());
            fout.flush();
            fout.close();

            out.println("Saving queue to file \"base.json\"");
            out.flush();
        } catch (Exception e) {
            // if (saving was fail) than: print exception and return.
            err.println(e.getMessage());
        }
    }

    /**
     * Read and execute the script from the specified file.
     * The script contains commands in the same format,
     * in which they are entered by the user in interactive mode.
     *
     * @param args arguments for the command.
     */
    @Override
    public void executeScript(String[] args) throws IOException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 1) {
            err.println("execute_script: this command has one easy parameter \"file's name\".");
            return;
        }

        // file's name
        String fileName = Parser.normalise(args[0]);

        // check recursion.
        if (set.contains(fileName)) {
            err.println("Recursion execution is fixed.");
            return;
        }

        // open file for reading.
        File file;
        BufferedReader fileReader;
        try {
            file = new File(fileName);
            fileReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            // if (file not found) than: print exception.
            err.println("File for reading not found.");
            return;
        }

        // push the input's stream to stack.
        stackReaders.push(in);
        // add the file's name to the set with same withes.
        set.add(fileName);
        // initialise the input's stream with file's input's stream of the file.
        in = fileReader;

        try {
            // declare line.
            String line;

            // read commands from the input's stream.
            do {
                // prompt to enter.
                out.print("$");
                out.flush();

                // if (the received end-symbol) than: get old input's stream.
                in.mark(1);
                if (in.read() == -1) {
                    break;
                }

                // else: read line from the stream and normalise this.
                in.reset();
                line = Parser.normalise(in.readLine());
                // if (the stream isn't console's stream of server) than: output the line to server's console.
                if (!stackReaders.isEmpty()) {
                    out.println(line);
                }
                // execute the command.
                runCommand(line);
            } while (true);
        } finally {
            // pop a stream from stack to the the stream.
            in = stackReaders.pop();
            // remove the file's name from the set.
            set.remove(args[0]);
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

        System.exit(0);
    }

    /**
     * Print and remove a head's spaceMarine of the queue.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void removeHead(String[] args) throws IOException {
        // if (the command has parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_head: this command hasn't parameters.");
            return;
        }

        // else if (the queue isn't empty): print and remove element from head of the collection.
        if (queue.size() > 0) { out.println(queue.poll()); }
    }

    /**
     * Add a new item to the collection if its value is less than the smallest item in this collection.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void addIfMin(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("add_if_min: this command has one composite parameter {element}.");
            return;
        }

        // else: read composite parameter (SpaceMarine).
        SpaceMarine spaceMarine = inputSpaceMarine();
        // if (the element is minimal) than: add the element to the collection.
        Optional<SpaceMarine> optional = queue.stream().min(SpaceMarine::compareTo);
        if (optional.equals(Optional.empty()) || spaceMarine.compareTo(optional.get()) < 0) {
            queue.add(spaceMarine);
        }
    }

    /**
     * Remove all items from the collection that exceed the specified value.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void removeGreater(String[] args) throws IOException {
        // if (the command has easy parameters) than: print exception and return.
        if (args == null || args.length != 0) {
            err.println("remove_greater: this command has one composite parameter {element}.");
            return;
        }

        // read composite parameter (SpaceMarine).
        SpaceMarine spaceMarine = inputSpaceMarine();
        // remove all elements from the collection greater than the element.
        Iterator<SpaceMarine> iter = queue.iterator();
        while (iter.hasNext()) {
            SpaceMarine sM = iter.next();
            if (sM.compareTo(spaceMarine) > 0) {
                iter.remove();
                return;
            }
        }
        queue.add(spaceMarine);
    }

    /**
     * Delete a single item from the collection,
     * the value of the field whose height is equivalent to the specified one.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void removeAnyByHeight(String[] args) throws IOException {
        // if (the command's parameters greater than one) than: print exception and return.
        if (args == null || args.length > 1) {
            err.println("remove_aby_by_height: this command has one easy \"Height\" or hasn't parameter.");
            return;
        }

        // else if (the command hasn't parameters) than: the command has one easy parameter "zero".
        if (args.length == 0) {
            Iterator<SpaceMarine> iter = queue.iterator();
            while (iter.hasNext()) {
                SpaceMarine sM = iter.next();
                if (sM.getHeight().equals(0)) {
                    iter.remove();
                    return;
                }
            }
            return;
        }

        // check the parameter (Integer)
        // and remove any element from the collection whose has a height equal to the height.
        try {
            int height = Integer.parseInt(args[0]);
            Iterator<SpaceMarine> iter = queue.iterator();
            while (iter.hasNext()) {
                SpaceMarine sM = iter.next();
                if (sM.getHeight().equals(height)) {
                    iter.remove();
                    return;
                }
            }
        } catch (NumberFormatException e) {
            // if (check is fail) than: print the exception and return.
            err.println("remove_any_by_height: the command's parameter is a number.");
        }
    }

    /**
     * Output the average value of the height field for all items in the collection.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void averageOfHeight(String[] args) throws IOException {
        // if (the command has parameters) than: print the exception and return.
        if (args == null || args.length != 0) {
            err.println("average_of_height: this command hasn't parameters.");
            return;
        }

        // else if (queue is empty) than: print zero.
        if (queue.isEmpty()) {
            out.println("The average value of the height: 0.");
        } else {
            // else: print the average value of the height.
            int result = queue.stream()
                    .mapToInt(sM -> sM.getHeight())
                    .reduce(0, Integer::sum);
            out.println("The average value of the height: " + ((double) result) / queue.size() + ".");
        }
    }

    /**
     * Output the count of elements whose <code>MeleeWeapon</code> value is greater than the specified value.
     *
     * @param args arguments for the command.
     */
    @Override
    public synchronized void countGreaterThanMeleeWeapon(String[] args) throws IOException {
        // if (the command hasn't only one easy parameter) than: print the exception and return.
        if (args == null || args.length != 1) {
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\".");
            return;
        }

        // else: check parameter(MeleeWeapon) and print count elements from the collection
        // with MeleeWeapon greater than the MeleeWeapon.
        try {
            MeleeWeapon meleeWeapon = MeleeWeapon.valueOf(args[0]);
            // if (check is correct): print the count.
            long count = queue.stream()
                    .filter(sM -> sM.getMeleeWeapon().compareTo(meleeWeapon) > 0)
                    .count();
            out.println(count + " queue's elements have the value \"Melee Weapon\", greater than the given value.");
        } catch (IllegalArgumentException e) {
            // else: print correct answers.
            err.println("count_greater_than_melee_weapon: this command has one easy parameter \"MeleeWeapon\" type of enumeration.");
        }
    }

    /**
     * Input <code>SpaceMarine</code>'s name with checking.
     *
     * @return the name of the space marine.
     */
    protected String inputName() throws IOException {
        while (true) {
            // input from the stream line, check the space marine's name and return this.
            try {
                out.println("Enter a name: ");
                String str = readConsoleLine();
                SpaceMarine.checkName(str);
                return str;
            } catch (IllegalArgumentException | NullPointerException e) {
                // if (received exception (not IO)) than: print the exception and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a X coordinate with checking.
     *
     * @return the X coordinate.
     */
    protected long inputX() throws IOException {
        while (true) {
            // read line from console, check (Long, Coordinates.checkX(x)) and return that.
            try {
                out.println("Enter a X: ");
                String str = readConsoleLine();
                Long x = Long.parseLong(str);
                Coordinates.checkX(x);
                return x;
            } catch (NumberFormatException e) {
                // if (The isn't number) than: print the exception and run the function again.
                err.println("Invalid input data format, re-enter.");
            } catch (IllegalArgumentException e) {
                // else if (received other exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a Y coordinate with checking.
     *
     * @return the Y coordinate.
     */
    protected long inputY() throws IOException {
        while (true) {
            try {
                // read line from the stream, check the line (Long, Coordinates.checkY(y)) and return that.
                out.println("Enter a Y: ");
                String str = readConsoleLine();
                Long y = Long.parseLong(str);
                Coordinates.checkY(y);
                return y;
            } catch (NumberFormatException e) {
                // if (the isn't number) than: print about that and run the function again.
                err.println("Invalid input data format, re-enter.");
            } catch (NullPointerException e) {
                // else if (received other exception (not IO)) than: print that and run the fuction again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input coordinates with checking.
     *
     * @return coordinates.
     */
    protected Coordinates inputCoordinates() throws IOException {
        while (true) {
            try {
                // run functions inputX() and inputY() with checking and return that.
                out.println("Enter coordinates: ");
                Coordinates coordinates = new Coordinates();
                coordinates.setX(inputX());
                coordinates.setY(inputY());
                return coordinates;
            } catch (IllegalArgumentException | NullPointerException e) {
                // if (received exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input count of health with checking.
     *
     * @return the count of health.
     */
    protected long inputHealth() throws IOException {
        while (true) {
            try {
                // read line from the stream, check (Long, SpaceMarine.checkHealth(health)) and return that.
                out.println("Enter the amount of health: ");
                String str = readConsoleLine();
                Long health = Long.parseLong(str);
                SpaceMarine.checkHealth(health);
                return health;
            } catch (NumberFormatException e) {
                // if (the isn't number) than: print about that and run the function again.
                err.println("Invalid input data format, re-enter.");
            } catch (IllegalArgumentException | NullPointerException e) {
                // else if (received other exception (not IO)) than: print that and run the function again.
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a height with checking.
     *
     * @return the height.
     */
    protected int inputHeight() throws IOException {
        while (true) {
            // read line from the stream, check (Integer, SpaceMarine.checkHeight(height)) and return that.
            try {
                out.println("Enter a height: ");
                String str = readConsoleLine();
                Integer height = Integer.parseInt(str);
                SpaceMarine.checkHeight(height);
                return height;
            } catch (NumberFormatException e) {
                // if (the isn't number) than: print about that and run the function again.
                err.println("Invalid input data format, re-enter.");
            }
        }
    }

    /**
     * Input astarte's category with checking.
     *
     * @return the astarte's category.
     */
    protected AstartesCategory inputCategory() throws IOException {
        while (true) {
            try {
                // print all categories.
                StringBuilder strB = new StringBuilder("Selected category:\r\n");
                for (AstartesCategory category : AstartesCategory.class.getEnumConstants()) {
                    strB.append(category + " ");
                }
                strB.setCharAt(strB.length() - 1, '.');
                out.println(strB.toString());
                // read line, check (AstartesCategory.check(category)) and return category.
                String str = readConsoleLine();
                AstartesCategory category = AstartesCategory.valueOf(str);
                AstartesCategory.check(category);
                return category;
            } catch (NullPointerException | IllegalArgumentException e) {
                // if (the isn't category) than: print exception about that and re-run the function.
                err.println("Invalid input data format, re-enter.");
            }
        }
    }

    /**
     * Input melee weapon with checking.
     *
     * @return the melee weapon.
     */
    protected MeleeWeapon inputMeleeWeapon() throws IOException {
        while (true) {
            try {
                // print all melee weapons.
                StringBuilder strB = new StringBuilder("Choose a melee weapon:\r\n");
                for (MeleeWeapon meleeWeapon : MeleeWeapon.values()) {
                    strB.append(meleeWeapon + " ");
                }
                strB.setCharAt(strB.length() - 1, '.');
                out.println(strB.toString());

                // read the melee weapon with checking.
                String str = readConsoleLine();
                MeleeWeapon weapon = MeleeWeapon.valueOf(str);
                MeleeWeapon.check(weapon);
                return weapon;
            } catch (IllegalArgumentException | NullPointerException e) {
                // if (the isn't melee weapon) than: print about that and re-run the function.
                err.println("Invalid input data format, re-enter.");
            }
        }
    }

    /**
     * Input a name of the Chapter with checking.
     *
     * @return the name of the chapter.
     */
    protected String inputNameOfChapter() throws IOException {
        while (true) {
            try {
                out.println("Enter the Chapter name: ");
                String str = readConsoleLine();
                Chapter.checkName(str);
                return str;
            } catch (IllegalArgumentException | NullPointerException e) {
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a name of the legion with checking.
     *
     * @return the name of the legion.
     */
    protected String inputParentLegion() throws IOException {
        do {
            out.println("Enter the name of the Legion: ");
            String str = readConsoleLine();
            Chapter.checkParentLegion(str);
            return str;
        } while (true);
    }

    /**
     * Input a count of marines.
     *
     * @return the merines' count.
     */
    protected int inputMarinesCount() throws IOException {
        while (true) {
            try {
                out.println("Enter the count of Marines: ");
                String str = readConsoleLine();
                Integer count = Integer.parseInt(str);
                Chapter.checkMarinesCount(count);
                return count;
            } catch (NumberFormatException e) {
                err.println("Invalid input data format, re-enter.");
            } catch (IllegalArgumentException e) {
                err.println(e.getMessage());
            }
        }
    }

    /**
     * Input a name of the world.
     *
     * @return the world's name.
     */
    protected String inputWorld() throws IOException {
        while (true) {
            out.println("Enter the name of the world: ");
            String str = readConsoleLine();
            Chapter.checkWorld(str);
            return str;
        }
    }

    /**
     * Input a chapter with checking.
     *
     * @return the chapter.
     */
    protected Chapter inputChapter() throws IOException {
        while (true) {
            Chapter chapter = new Chapter();
            out.println("Enter a Chapter: ");
            chapter.setName(inputNameOfChapter());
            chapter.setParentLegion(inputParentLegion());
            chapter.setMarinesCount(inputMarinesCount());
            chapter.setWorld(inputWorld());
            return chapter;
        }
    }

    /**
     * Input a space marine.
     *
     * @return the space marine.
     */
    protected SpaceMarine inputSpaceMarine() throws IOException {
        while (true) {
            SpaceMarine spaceMarine = new SpaceMarine();
            spaceMarine.setName(inputName());
            spaceMarine.setCoordinates(inputCoordinates());
            spaceMarine.setHealth(inputHealth());
            spaceMarine.setHeight(inputHeight());
            spaceMarine.setCategory(inputCategory());
            spaceMarine.setMeleeWeapon(inputMeleeWeapon());
            spaceMarine.setChapter(inputChapter());
            return spaceMarine;
        }
    }

    /**
     * Read line from the input's stream and
     * if the line isn't from the server's console than print the normal line to server's console.
     *
     * @return the string after normalization.
     */
    protected String readConsoleLine() throws IOException {
        // print about successful input from the console.
        out.print("$");
        out.flush();

        // if (the received end-symbol) than: execute command exit.
        // out symbol to prompt you to enter.
        in.mark(1);
        if (in.read() == -1) {
            err.println("Received the program end symbol.");
            runCommand("exit");
        }

        // else: read line from server's console.
        in.reset();
        // normalization the line.
        String str = Parser.normalise(in.readLine());

        // if (script's execute) than: print the normal commands to server's console.
        if (!stackReaders.isEmpty()) {
            out.println(str);
        }
        return str;
    }
}