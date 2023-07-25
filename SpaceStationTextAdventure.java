/**
 * Text adventure where you have to divert the space station from collision course
 * with a meteor
 *
 * @author Ritesh Ravji
 * @version 17/7/23
 */

import java.util.Scanner; // Read keyboard
import java.util.Arrays; // Flexible sized array
import java.util.ArrayList; // Flexible sized array
import java.io.File; // Get files
import java.io.IOException; // Handle file exceptions

import java.util.Dictionary; // Save room data in dictionary
import java.util.Hashtable; // Goes with Dictionary
import java.util.Enumeration; // Goes with Dictionary AND for making enums

public class SpaceStationTextAdventure
{
    // Create list which contains list of possible directions to check whether a direction is possible
    final String[] DIRECTIONSLIST = {"north", "south", "east", "west", "up", "down"};
    String currentRoom;
    
    enum CommandType {
        DIRECTION,
        DESCRIPTION,
        PICKUP,
        GET,
        DROP,
        INTERACT,
        USE,
        HELP,
        SETTING
    }
    
    enum OnInteract {
        HINT, // like a description but for when in specific rooms
        TELEPORT, // move to a different room
        COMPLETEGAME // complete game
    }
    
    Scanner keyboard = new Scanner(System.in);
    
    // Create a new dictionary to store descriptions
    Dictionary<String, String> descriptionDictionary;
    // Dictionary for rooms (only used to check if a room exists)
    Dictionary<String, Boolean> roomDictionary = new Hashtable<>();
    // Dictionary for directions
    Dictionary<String, Dictionary> directionDictionary = new Hashtable<>();
    // Dictionary for items
    Dictionary<String, ArrayList<String>> itemsDictionary = new Hashtable<>();
    // Dictionary for interactables
    Dictionary<String, Dictionary> interactDictionary = new Hashtable<>();
    // Dictionary for item descriptions
    Dictionary<String, String> itemDescriptionDictionary = new Hashtable<>();
    // Dictionary for ascii colour codes
    Dictionary<String, String> coloursDictionary = new Hashtable<>();
    // Dictionary with text explanation for a new room
    Dictionary<String, String> newRoomDictionary = new Hashtable<>();
    // Dictionary with enum and extra info from use items
    Dictionary<String, String[]> useItemsDictionary = new Hashtable<>();
    // dictionary with keys like introduction and game complete can be stored as file and then in dictionary when run
    Dictionary<String, String> configurations = new Hashtable<>();
    
    void print(String str) {
        // if print method is called with one parameter, this method is run
        // print is 13 characters shorter than System.out.println. There are probably 50+ print functions, saves more than 650 characters
        System.out.println(str);
    }
    
    // method overloading
    void print(String str, String colour) {
        // if print method is called with two parameters, this method is run
        if (System.console() != null && Boolean.parseBoolean(configurations.get("colours"))) {
            // run if true, do not run if false or null (not found in config folder)
            System.out.println(getColour(colour) + str + getColour("RESET"));
        } else {
            // no console attached, might be running in a program like BlueJ
            System.out.println(str);
        }
    }
    
    // method overloading for printing items in a room
    void print(ArrayList str, String colour) {
        // if print method is called with two parameters, this method is run
        if (System.console() != null && Boolean.parseBoolean(configurations.get("colours"))) {
            // run if true, do not run if false or null (not found in config folder)
            System.out.println(getColour(colour) + str + getColour("RESET"));
        } else {
            // no console attached, might be running in a program like BlueJ
            System.out.println(str);
        }
    }
    
    void clearScreen() {
        // clears BlueJ and command prompt window
        try {
            // clear screen
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch(IOException error) {
            print("error occured when clearing screen", "RED");
            print(error.getClass().getCanonicalName(), "RED");
        } catch(InterruptedException error) {
            print("error occured when clearing screen", "RED");
            print(error.getClass().getCanonicalName(), "RED");
        }
    }
    
    boolean startingRoomExists() {
        boolean success = true;
        String configStartingRoom = configurations.get("startingRoom");
        if (configStartingRoom != null) {
            if (roomDictionary.get(configStartingRoom) == null) {
                print("starting room file was found but it does not exist", "RED");
                print("error found at config/startingRoom", "RED");
                success = false;
            }
        } else {
            print("starting room folder not found in config folder", "RED");
            success = false;
        }
        return success;
    }
    
    // Methods to do with configurations
    
    void applyConfigurations() {
        // get the folder for configurations
        File configFolder = new File("config");
        if (configFolder.exists()) {
            // Create a list of contained files
            File[] configFiles = configFolder.listFiles();
            for (File propertyFile: configFiles) {
                String propertyName = propertyFile.getName();
                // delete .txt from name
                propertyName = propertyName.substring(0, propertyName.length()-4);
                Scanner readFile;
                try {
                    // Safely open the file
                    readFile = new Scanner(propertyFile);
                } catch (IOException error) {
                    print("failed to load configuration for " + propertyName, "YELLOW");
                    print(error.getClass().getCanonicalName(), "RED");
                    //error.printStackTrace();
                    // continue because opening has failed and non essential part of text adventure
                    continue;
                }
                String text = "";
                while (readFile.hasNextLine()) {
                    text += readFile.nextLine();
                    // need to add newline character or it will be one big line
                    // check if another line otherwise there will be a newline at end of string
                    if (readFile.hasNextLine()) {
                        text += "\n";
                    }
                }
                configurations.put(propertyName, text);
            }
        } else {
            // continue because opening has failed and non essential part of text adventure
            print("WARNING config folder not found", "YELLOW");
            print("make sure the config folder is in the same directory as the project file. Many features may be inaccessible", "YELLOW");
            waitForInput();
        }
    }
    
    // Methods to do with using items
    
    void addUseItem(String itemName, String enumType, String roomName, String extraInfo) {
        // room name is the related room, for example: if enumType: TELEPORT, room name is space (teleport from space with item)
        // extra info depends on enum type:
        // if enum is TELEPORT then extra info is a description of teleport
        // if enum is COMPLETEGAME then extra info is not needed
        boolean enumExists = false;
        try {
            OnInteract.valueOf(enumType); // check that the enum type exists
            enumExists = true;
        } catch (IllegalArgumentException e) {
            // Enum does not exist
            print("An error occured with an item", "YELLOW");
            print("it may be impossible to complete the game", "YELLOW");
            //System.out.println(e);
        }
        if (enumExists) {
            String[] contents = {enumType, roomName, extraInfo};
            useItemsDictionary.put(itemName, contents);
        }
        return;
    }
    
    void applyUseItems() {
        // on program init
        // get the folder for use items
        File useItemsFolder = new File("UseItems");
        if (useItemsFolder.exists()) {
            // Create a list of contained files
            File[] useItemsFiles = useItemsFolder.listFiles();
            for (File itemFile: useItemsFiles) {
                String itemName = itemFile.getName();
                // delete .txt from name
                itemName = itemName.substring(0, itemName.length()-4);
                Scanner readFile;
                try {
                    // Safely open the file
                    readFile = new Scanner(itemFile);
                } catch (IOException error) {
                    print("failed to load item actions for " + itemName, "YELLOW");
                    print(error.getClass().getCanonicalName(), "RED");
                    //error.printStackTrace();
                    // continue because opening has failed and non essential part of text adventure
                    continue;
                }
                int line = 0;
                String enumType = ""; // what enum (HINT, TELEPORT, etc)
                String roomName = ""; // where to teleport or give hint
                String extraInfo = "";
                
                while (readFile.hasNextLine()) {
                    line++;
                    if (line == 1) {
                        enumType = readFile.nextLine();
                    } else if (line == 2) {
                        roomName = readFile.nextLine();
                    } else {
                        // need to add newline character or it will be one big line
                        extraInfo += readFile.nextLine();
                        if (readFile.hasNextLine()) {
                            // only add newline if there is another line
                            extraInfo += "\n";
                        }
                    }
                }
                addUseItem(itemName, enumType, roomName, extraInfo);
            }
        } else {
            // continue because opening has failed and non essential part of text adventure
            print("WARNING UseItems folder not found", "YELLOW");
            print("make sure the UseItems folder is in the same directory as the project file. Some features may be inaccessible", "YELLOW");
            waitForInput();
        }
    }
    
    String[] getUseItem(String itemName) {
        return useItemsDictionary.get(itemName);
    }
    
    // Methods to do with new rooms
    void addRoomSequence(String room, String toPrint) {
        // toPrint: what to print on entering room
        newRoomDictionary.put(room, toPrint);
    }
    
    void applyRoomSequences() {
        // should run when program is initiated
        // get the folder for room sequences
        File roomSequenceFolder = new File("NewRoomSequence");
        if (roomSequenceFolder.exists()) {
            // Create a list of contained files
            File[] roomSequenceFiles = roomSequenceFolder.listFiles();
            for (File roomFile: roomSequenceFiles) {
                String roomName = roomFile.getName();
                // delete .txt from name
                roomName = roomName.substring(0, roomName.length()-4);
                Scanner readFile;
                try {
                    // Safely open the file
                    readFile = new Scanner(roomFile);
                } catch (IOException error) {
                    print("failed to load sequence for " + roomName, "YELLOW");
                    print(error.getClass().getCanonicalName(), "RED");
                    //error.printStackTrace();
                    // continue because opening has failed and non essential part of text adventure
                    continue;
                }
                String sequence = "";
                while (readFile.hasNextLine()) {
                    // need to add newline character or it will be one big line
                    sequence += readFile.nextLine() + "\n";
                }
                addRoomSequence(roomName, sequence);
            }
        } else {
            // continue because opening has failed and non essential part of text adventure
            print("WARNING NewRoomSequence folder not found", "YELLOW");
            print("make sure the NewRoomSequence folder is in the same directory as the project file. Some features may be inaccessible", "YELLOW");
            waitForInput();
        }
    }
    
    String hasRoomSequence(String room) {
        // returns string to print if exists
        // returns null if does not exist
        return newRoomDictionary.get(room);
    }
    
    // Methods to do with colours
    
    void initColours() {
        // Add pre-defined ansi colour codes
        coloursDictionary.put("BLACK", "\u001B[30m");
        coloursDictionary.put("RED", "\u001b[31m"); // use for errors
        coloursDictionary.put("GREEN", "\u001b[32m");
        coloursDictionary.put("YELLOW", "\u001b[33m"); // use for warnings
        coloursDictionary.put("BLUE", "\u001b[34m");
        coloursDictionary.put("MAGENTA", "\u001B[35m");
        coloursDictionary.put("CYAN", "\u001b[36m");
        coloursDictionary.put("WHITE", "\u001b[37m");
        coloursDictionary.put("RESET", "\u001b[0m"); // stop ansi colouring
    }
    
    String getColour(String colour) {
        return coloursDictionary.get(colour);
    }

    // Methods to do with directions
    
    void addDirection(String room, String direction, String leadsTo) {
        // Create a new direction in a room
        // Intended to be run only during program start OR when an interactable creates a new direction
        directionDictionary.get(room).put(direction, leadsTo);
    }
    
    void delDirection(String room, String direction) {
        // Delete a direction in a room
        // Intended to be used with interactables
        directionDictionary.get(room).remove(direction);
    }
    
    
    String roomInDirection(String room, String direction) {
        // Check if room is in a direction and return room name or return null
        String leadsTo = (String) directionDictionary.get(room).get(direction);
        return leadsTo;
    }
    
    void printDirections() {
        // Get dictionary with every possible direction in current room
        Dictionary roomDictionary = directionDictionary.get(currentRoom);
        
        print("You can move:");
        Enumeration<String> directions = roomDictionary.keys();
        while (directions.hasMoreElements()) {
            // Get dictionary key (direction)
            String roomDirection = directions.nextElement();
            // Get dictionary value (destination)
            String leadsTo = (String) directionDictionary.get(currentRoom).get(roomDirection);
            print(roomDirection + " to " + leadsTo, "CYAN");
        }
        print("");
    }
    
    boolean moveDir(String direction) {
        boolean success = false;
        
        String room = roomInDirection(currentRoom, direction);
        // null means no room in direction and return movement failed
        // if not null, then change the current room and return movement success
        if (room != null) {
            if (roomDictionary.get(room) == null) {
                print("tried to move to a room that does not exist", "RED");
            } else {
                currentRoom = room;
                success = true;
            }
        }
        return success;
    }
    
    boolean applyDirectionsToRoom(String roomName) {
        String folderName = roomName; // folder name is same as room name
        directionDictionary.put(roomName, new Hashtable<>());
        // Get the file with directions
        File directionsFile = new File("Rooms\\"+folderName+"\\directions.txt");
        Scanner readFile;
        try {
            // Safely open the file
            readFile = new Scanner(directionsFile);
        } catch (IOException error) {
            print("Could not open the file containing directions for " + roomName, "RED");
            print("file path: "+directionsFile.getAbsolutePath(), "RED");
            print(error.getClass().getCanonicalName(), "RED");
            error.printStackTrace();
            // stop because directions are an essential part of the program
            return false;
        }
        while (readFile.hasNextLine()) {
            // New directions dictionary for each room
            String line = readFile.nextLine();
            String[] splitLine = line.split(" ");
            if (splitLine.length == 2) {
                // First word of each line is direction
                // Second word of each line is the destination
                String startingRoom = roomName;
                String direction = splitLine[0];
                String destination = splitLine[1];
                addDirection(startingRoom, direction, destination);
            }
        }
        return true; // success
    }
    
    // Description methods
    
    void readDescription(String room) {
        String roomDescription = descriptionDictionary.get(currentRoom);
        print("");
        print(roomDescription, "YELLOW");
        print("");
    }
    
    void applyDescriptionToRoom(String roomName) {
        String folderName = roomName; // folder name is same as room name
        File descriptionFile = new File("Rooms\\"+folderName+"\\description.txt");
        Scanner readFile = null;
        try {
            // Safely open the file
            readFile = new Scanner(descriptionFile);
        } catch (IOException error) {
            print("failed to load description for " + roomName, "YELLOW");
            print(error.getClass().getCanonicalName(), "RED");
            // continue because opening has failed and non essential part of text adventure
        }
        String roomDesc = ""; // room description as string
        if (readFile == null) {
            roomDesc = "no description found";
        } else {
            while (readFile.hasNextLine()) {
                roomDesc+=readFile.nextLine();
            }
        }
        descriptionDictionary.put(roomName, roomDesc);
    }
    
    void applyItemDescriptions() {
        // runs during program start
        
        // get the folder for item descriptions
        File itemDescriptFolder = new File("ItemDescriptions");
        if (itemDescriptFolder.exists()) {
            // Create a list of contained files
            File[] itemDescriptFiles = itemDescriptFolder.listFiles();
            
            for (int i = 0; i < itemDescriptFiles.length; i++) {
                String itemName = itemDescriptFiles[i].getName();
                // delete .txt from name
                itemName = itemName.substring(0, itemName.length()-4);
                File currentItem = itemDescriptFiles[i];
                Scanner readFile;
                try {
                    // Safely open the file
                    readFile = new Scanner(currentItem);
                } catch (IOException error) {
                    print("failed to load description for " + itemName, "YELLOW");
                    print(error.getClass().getCanonicalName(), "RED");
                    //error.printStackTrace();
                    // continue because opening has failed and non essential part of text adventure
                    continue;
                }
                int lineNum = 0;
                String description = "";
                while (readFile.hasNextLine()) {
                    // need to add newline character or it will be one big line
                    description += "\n"+readFile.nextLine();
                }
                itemDescriptionDictionary.put(itemName, description);
            }
        } else {
            // continue because opening has failed and non essential part of text adventure
            print("WARNING ItemDescriptions folder not found", "YELLOW");
            print("make sure the ItemDescriptions folder is in the same directory as the project file. Some features may be inaccessible", "YELLOW");
            waitForInput();
        }
    }
    
    void readItemDescription(String item) {
        String itemDescription = itemDescriptionDictionary.get(item);
        print("");
        if (itemDescription == null) {
            print("item description not found", "RED");
        } else {
            print(itemDescription, "YELLOW");
        }
        print("");
    }
    
    // Interact methods
    
    void addInteract(String room, String interactName, String direction, String startRoom, String leadsTo, String enabledText, String disabledText) {
        // method adds item to room
        
        // Get all current items in room
        Dictionary<String, String[]> roomInteractables= interactDictionary.get(room);
        
        String[] interactInfo = {startRoom, leadsTo, direction, enabledText, disabledText};
        roomInteractables.put(interactName, interactInfo);
        // don't need to set item array to dictionary as it is already changed when added item
    }
    
    boolean applyInteracts() {
        // get the folder for interactables
        File interactablesFolder = new File("Interactables");
        if (interactablesFolder.exists()) {
            // Create a list of contained files
            File[] interactablesFiles = interactablesFolder.listFiles();
            Scanner readFile;
            for (int i = 0; i < interactablesFiles.length; i++) {
                String interactableName = interactablesFiles[i].getName();
                // delete .txt from name
                interactableName = interactableName.substring(0, interactableName.length()-4);
                File currentInteractable = interactablesFiles[i];
                try {
                    // Safely open the file
                    //if (true)
                        //throw new IOException();
                    readFile = new Scanner(currentInteractable);
                } catch (IOException error) {
                    print("Could not open the file containing interactable " + interactableName, "RED");
                    print("file path: "+currentInteractable.getAbsolutePath(), "RED");
                    print(error.getClass().getCanonicalName(), "RED");
                    error.printStackTrace();
                    // stop because directions are an essential part of the program
                    return false;
                }
                int lineNum = 0;
                String enabledText = null; // BlueJ does not like it when you don't initalise a value to String variables
                String disabledText = null;
                String room = null;
                String startRoom = null;
                String leadsTo = null;
                String direction = null;
                while (readFile.hasNextLine()) {
                    String line = readFile.nextLine();
                    lineNum++;
                    if (lineNum == 1) {
                        // comments in file
                        continue;
                    } else if (lineNum == 2) {
                        // activated text
                        enabledText = line;
                    } else if (lineNum == 3) {
                        // deactivated text
                        disabledText = line;
                    } else if (lineNum == 4) {
                        // room to place
                        room = line;
                    } else if (lineNum == 5) {
                        // start room
                        startRoom = line;
                    } else if (lineNum == 6) {
                        // room unlocks
                        leadsTo = line;
                    } else if (lineNum == 7) {
                        // direction from start room
                        direction = line;
                    }
                }
                // check each variable has a value
                if (enabledText != null && disabledText != null && room != null && startRoom != null && leadsTo != null && direction != null) {
                    addInteract(room, interactableName, direction, startRoom, leadsTo, enabledText, disabledText);
                } else {
                    print("an error occurred with an interactable", "RED");
                }
            }
        } else {
            print("ERROR: no interactable files were found", "RED");
            print("make sure there is a folder 'Interactables' in the same directory", "RED");
            // stop because directions are an essential part of the program
            return false;
        }
        return true; // program success!
    }
    
    void printInteractsInRoom(String room) {
        Dictionary roomInteractables = interactDictionary.get(room);
        Enumeration <String> interactables = roomInteractables.keys();
        
        // if statement otherwise will just say "You can interact with:" then empty if no keys in dictionary
        if (roomInteractables.size() > 0) {
            print("You can interact with:");
            while (interactables.hasMoreElements()) {
                String key = interactables.nextElement();
                print(key, "CYAN");
            }
            print(""); // formatting
        }
    }
    
    String[] getInteractInRoom(String room, String interactName) {
        Dictionary<String, String[]> roomInteractables = interactDictionary.get(room);
        String[] interactInfo = roomInteractables.get(interactName.toLowerCase());
        return interactInfo;
    }
    
    // Inventory methods
    
    void addItem(String room, String item) {
        // method adds item to room
        
        // Get all current items in room
        ArrayList<String> roomItems = itemsDictionary.get(room);
        roomItems.add(item);
        // don't need to set item array to dictionary as it is already changed when added item
    }
    
    boolean removeItem(String room, String item) {
        boolean success = false;
        ArrayList<String> roomItems = itemsDictionary.get(room);
        int itemIndex = roomItems.indexOf(item);
        // return value -1 means not found
        if (itemIndex != -1) {
            roomItems.remove(itemIndex);
            success = true;
        }
        return success;
    }
    
    void addInventory(String item) {
        // add item to dummy room Inventory
        addItem("Inventory", item);
    }
    
    boolean removeInventory(String item) {
        // remove item from dummy room Inventory
        return removeItem("Inventory", item);
    }
    
    boolean hasItem(String item) {
         // check if dummy room Inventory contains item
        ArrayList<String> roomItems = itemsDictionary.get("Inventory");
        return roomItems.contains(item);
    }
    
    void printItemsInRoom(String room) {
        // list all items in dummy room
        ArrayList<String> roomItems = itemsDictionary.get(room);
        print("Items in this room:");
        
        print(roomItems, "CYAN");
    }
    
    void printInventory() {
        ArrayList<String> roomItems = itemsDictionary.get("Inventory");
        print("Items in inventory:");
        // System.out.println(roomItems); just printing array list will display items in square brackets
        for (String item: roomItems) {
            print(item, "CYAN");
        }
    }
    
    boolean applyItemsToRoom(String roomName) {
        String folderName = roomName; // folder name is same as room name
        File itemsFile = new File("Rooms\\"+folderName+"\\items.txt");
        Scanner readFile;
        try {
            // Safely open the file
            readFile = new Scanner(itemsFile);
        } catch (IOException error) {
            print("Could not open the file containing item for " + roomName, "RED");
            print("file path: "+itemsFile.getAbsolutePath(), "RED");
            print(error.getClass().getCanonicalName(), "RED");
            error.printStackTrace();
            // stop because directions are an essential part of the program
            return false;
        }
        // New ArrayList to store items
        itemsDictionary.put(roomName, new ArrayList<String>());
        while (readFile.hasNextLine()) {
            // each line is an item name
            String line = readFile.nextLine();
            // add item to dictionary
            addItem(roomName, line);
        }
        return true; // success
    }
    
    // Instruction methods
    void waitForInput() {
        print("press enter to continue", "YELLOW");
        keyboard.nextLine();
    }
    
    void howToPlay() {
        print("There are 6 possible directions:");
        // print each element in DIRECTIONSLIST
        for (String direction: DIRECTIONSLIST) {
            print(direction, "CYAN");
        }
        waitForInput();
        print("to get the description of the current room, type 'description'");
        print("for the description of an item, type the 'description ' and items name");
        waitForInput();
        print("pick up an item with, 'pickup ' and item name");
        print("drop an item with 'drop ' and item name");
        print("use an item with 'use ' and item name");
        waitForInput();
        print("to interact, type 'interact ' and item name");
        waitForInput();
        print("to toggle clear screen, type 'setting clearScreen (true/false)'");
        waitForInput();
        if (System.console() != null) {
            // console attached
            print("if the text on screen is odd (random square brackets), try disabling coloured text");
            print("to toggle coloured text, type 'setting colours (true/false)'");
            waitForInput();
        }
        print("if you need to see the command list again, type 'help'", "MAGENTA");
        waitForInput();
    }
    
    void introduction() {
        String intro = configurations.get("introduction");
        String[] introArr = intro.split("\n");
        
        for (String line: introArr) {
            if (line.equals("waitForInput();")) {
                waitForInput();
            } else {
                print(line);
            }
        }
    }
    
    // Command methods
    
    // Return result of returnCommand
    class CommandResult {
        CommandType type;
        String instructions;
        CommandResult(CommandType enumCommandType, String commandInstructions) {
            this.type = enumCommandType;
            this.instructions = commandInstructions;
        }
    }
    
    CommandResult returnCommand() {
        // Return type class so can fit two different types of variables
        String commandInstruction = "";
        CommandType commandType = null;
        boolean validInput = false;
        while (!validInput) {
            print("Input a command", "GREEN");
            String userInput = keyboard.nextLine();
            
            // Checks if input is a direction
            // if user types "north" move north, not "move north"
            boolean isDirection = Arrays.asList(DIRECTIONSLIST).contains(userInput.toLowerCase());
            if (isDirection) {
                commandInstruction = userInput.toLowerCase(); 
                commandType = CommandType.DIRECTION;
                validInput = true;
            } else {
                // Shortcuts
                switch(userInput.toUpperCase()) {
                    case "N":
                        commandInstruction = "north"; 
                        commandType = CommandType.DIRECTION;
                        validInput = true;
                        break;
                    case "S":
                        commandInstruction = "south"; 
                        commandType = CommandType.DIRECTION;
                        validInput = true;
                        break;
                    case "E":
                        commandInstruction = "east"; 
                        commandType = CommandType.DIRECTION;
                        validInput = true;
                        break;
                    case "W":
                        commandInstruction = "west"; 
                        commandType = CommandType.DIRECTION;
                        validInput = true;
                        break;
                    case "U":
                        commandInstruction = "up"; 
                        commandType = CommandType.DIRECTION;
                        validInput = true;
                        break;
                    case "D":
                        commandInstruction = "down"; 
                        commandType = CommandType.DIRECTION;
                        validInput = true;
                        break;
            
                }
            }
            
            if (validInput) {
                // restart loop and exit as loop requirements no longer met
                continue;
            }
            
            // more cleaner way to iterate though enums and compare
            
            for (CommandType type: CommandType.values()) {
                String referenceString = type.name();
                
                // Get minimum of userInput length or referenceString length
                // otherwise IndexOutOfBounds exception if referenceString.length() > userInput.length()
                int minimumChars = Math.min(userInput.length(), referenceString.length());
                String startingChars = userInput.toUpperCase().substring(0, minimumChars);
                if (startingChars.equals(referenceString)) {
                    String item = "";
                    if (userInput.length() >= referenceString.length()+1) {
                        item = userInput.substring(referenceString.length()+1); // length of comparison word + space
                        // .substring will error if just typed "DROP" and not "DROP " unless you check with if statement first
                    }
                    commandInstruction = item;
                    commandType = type;
                    validInput = true;
                    continue;
                }
            }
            
            if (!validInput) {
                print("Not a command", "RED");
            }
        }
        CommandResult returnPackage = new CommandResult(commandType, commandInstruction);
        return returnPackage;
    }
    public static void main(String[] args) {
        // will run when accessed from .jar file
        // run text adventure as class from separate main function to get around accessing non-static variables and methods from static context
        SpaceStationTextAdventure adventure = new SpaceStationTextAdventure();
    }
    /**
     * Constructor for objects of class Space_Station_Text_Adventure
     */
    public SpaceStationTextAdventure()
    {
        clearScreen();
        applyConfigurations();
        initColours();
        
        descriptionDictionary = new Hashtable<>();
        // INITALISE VARIABLES
        // get the file for rooms
        File roomFolder = new File("Rooms");
        // Create a list of contained files
        File[] roomsFiles = roomFolder.listFiles();
        if (roomsFiles == null) {
            print("ERROR: no room files were found", "RED");
            print("make sure there is a folder 'Rooms' in the same directory", "RED");
            return;
        }
        // used to check if methods are successful
        boolean success;
        
        for (int fileIndex = 0; fileIndex < roomsFiles.length; fileIndex++) {
            String fileName = roomsFiles[fileIndex].getName();
            String roomName = fileName;
            // add true to dictionary to show room exists in program
            roomDictionary.put(roomName, true);
            // functions that return a success boolean are functions which are required to run successfully
            success = applyDirectionsToRoom(roomName);
            if (!success) {
                return;
            }
            
            applyDescriptionToRoom(roomName); // don't check for success because can still use program without room descriptions
            
            success = applyItemsToRoom(roomName);
            if (!success) {
                return;
            }
            
            // Add each room to interactDictionary
            interactDictionary.put(roomName, new Hashtable<>());
        }
        // Fake room inventory
        itemsDictionary.put("Inventory", new ArrayList<String>());
        
        success = applyInteracts();
        if (!success) {
            return; // program failure
        }
        
        applyItemDescriptions(); // add information to the program about item descriptions
        applyRoomSequences(); // add information to the program about using room sequences from the RoomSequences folder
        applyUseItems(); // add information to the program about using items from the UseItems folder
        
        success = startingRoomExists();
        if (!success) {
            return;
        }
        final String STARTINGROOM = configurations.get("startingRoom");
        currentRoom = STARTINGROOM;
        
        // INSTRUCTIONS
        if (true) {
            clearScreen();
            howToPlay();
            clearScreen();
            introduction();
            clearScreen();
        }
        
        
        // START MAIN GAME LOOP
        
        boolean gameComplete = false;
        
        while (!gameComplete) {
            // Separator between last action
            print("=".repeat(25), "GREEN");
            print("You are currently in " + currentRoom, "GREEN");
            print("");
            printInteractsInRoom(currentRoom);
            printDirections();
            printItemsInRoom(currentRoom);
            printInventory();
            CommandResult command = returnCommand();
            CommandType commandType = command.type; // enum
            String commandInstruction = command.instructions; // extra instructions
            if (Boolean.parseBoolean(configurations.get("clearScreen"))) {
                /* if clearScreen == "true":
                 * clear screen
                 * else clearScreen == "false" or null (not found in config folder, turns into false in parseBoolean):
                 * continue program
                 */
                clearScreen();
            }
            if (commandType == CommandType.DIRECTION) {
                String direction = commandInstruction;
                
                success = moveDir(direction);
                if (success) {
                    print("Moving " + direction, "YELLOW");
                } else {
                    print("No room in this direction!", "RED");
                }
                // Check for "room sequence", like a cutscene to tell the player extra useful info
                String sequence = hasRoomSequence(currentRoom);
                if (sequence != null) {
                    // exists
                    print(""); // formatting
                    print(sequence, "YELLOW");
                    waitForInput(); 
                }
            } else if (commandType == CommandType.DESCRIPTION) {
                if (commandInstruction == "") {
                    // empty so print room description
                    readDescription(currentRoom);
                } else if (hasItem(commandInstruction)) {
                    // not empty so print item description if is holding it
                    readItemDescription(commandInstruction);
                } else {
                    print("You do not have this item...", "YELLOW");
                }
            } else if (commandType == CommandType.PICKUP || commandType == CommandType.GET) {
                String item = commandInstruction;
                success = removeItem(currentRoom, item);
                if (success) {
                    print("The item was added to inventory!", "YELLOW");
                    addInventory(item);
                } else {
                    print("The item does not exist!", "RED");
                }
            } else if (commandType == CommandType.DROP) {
                String item = commandInstruction;
                success = removeInventory(item);
                if (success) {
                    print("The item was dropped!", "YELLOW");
                    addItem(currentRoom, item);
                } else {
                    print("The item does not exist!", "RED");
                }
            } else if (commandType == CommandType.USE) {
                String object = commandInstruction;
                
                // check the user is using an item they are actually holding
                if (hasItem(object)) {
                    String[] useItemInfo = getUseItem(object);
                    if (useItemInfo != null) {
                        OnInteract itemEnum = OnInteract.valueOf(useItemInfo[0]); // get enum as string and change to enum
                        String roomName = useItemInfo[1];
                        String extraInfo = useItemInfo[2];
                        if (itemEnum == OnInteract.COMPLETEGAME && currentRoom.equals(roomName)) {
                            gameComplete = true;
                        } else if (itemEnum == OnInteract.TELEPORT && currentRoom.equals(roomName)) {
                            print(extraInfo, "YELLOW"); // info like "you teleported!" to user
                            removeInventory(object);
                            currentRoom = STARTINGROOM;
                        } else if (itemEnum == OnInteract.HINT && currentRoom.equals(roomName)) {
                            print(extraInfo, "YELLOW");
                        } else {
                            // more likely to be error but just incase
                            print("It did nothing", "YELLOW");
                        }
                    } else {
                        // no data so does nothing
                        print("It did nothing", "RED");
                    }
                } else {
                    // Not holding this
                    print("You are not holding this...", "RED");
                }
            } else if (commandType == CommandType.INTERACT) {
                String object = commandInstruction;
                String[] info = getInteractInRoom(currentRoom, object);
                if (info == null) {
                    // not found
                    print("The object does not exist", "RED");
                } else {
                    String startingRoom = info[0];
                    String leadsTo = info[1];
                    String direction = info[2];
                    String enabledText = info[3];
                    String disabledText = info[4];
                    print(""); // formatting
                    // toggles directions
                    // IF: pathway exists between starting room and destination
                    // delete the pathway (e.g. gravity turned on, cannot float to room above)
                    // ELSE: (pathway does not exist between starting room and destination)
                    // create pathway (e.g. gravity turned off, can float to room above)
                    if (roomInDirection(startingRoom, direction) != null) {
                        // already exists so revert changes
                        print(disabledText, "YELLOW");
                        delDirection(startingRoom, direction);
                    } else {
                        print(enabledText, "YELLOW");
                        addDirection(startingRoom, direction, leadsTo);
                    }
                    print(""); // formatting
                }
            } else if (commandType == CommandType.HELP) {
                howToPlay();
            } else if (commandType == CommandType.SETTING) {
                String[] settingArr = commandInstruction.split(" ");
                if (settingArr.length == 2) {
                    String setting = settingArr[0]; // setting to change
                    String value = settingArr[1]; // new setting value
                    if (configurations.get(setting) != null) {
                        // setting already has a value so change
                        configurations.put(setting, value);
                        print("setting changed", "YELLOW");
                    } else {
                        // setting not found
                        print("no setting found", "RED");
                    }
                } else {
                    print("incorrect parameters, please use setting 'setting name' 'setting value'", "RED");
                }
            } else {
                // Idealy would never occur but just in case
                print("an error occured", "RED");
            }
        }
        print(configurations.get("gameCompleteText"), "GREEN"); // print text stored in config/gameCompleteText.txt which is stored in configrations in program
    }
}
