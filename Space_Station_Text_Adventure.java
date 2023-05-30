/**
 * Text adventure where you have to divert the space station from collision course
 * with a meteor
 *
 * @author Ritesh Ravji
 * @version 30/4/23
 */

import java.util.Scanner; // Read keyboard
import java.util.Arrays; // Flexible sized array
import java.util.ArrayList; // Flexible sized array
import java.io.File; // Get files
import java.io.IOException; // Handle file exceptions

import java.util.Dictionary; // Save room data in dictionary
import java.util.Hashtable; // Goes with Dictionary
import java.util.Enumeration; // Goes with Dictionary AND for making enums

public class Space_Station_Text_Adventure
{
    // Create list which contains list of possible directions to check whether a direction is possible
    String[] DIRECTIONSLIST = {"north", "south", "east", "west", "up", "down"};
    String currentRoom = "Entrance"; // Starting room
    
    enum CommandType {
        DIRECTION,
        DESCRIPTION,
        PICKUP,
        DROP,
        INTERACT,
        USE,
        HELP
    }
    
    enum OnInteract {
        HINT,
        TELEPORT,
        COMPLETEGAME
    }
    
    Scanner keyboard = new Scanner(System.in);
    
    // Create a new dictionary to store descriptions
    Dictionary<String, String> descriptionDictionary = new Hashtable<>();
    // Dictionary for directions
    Dictionary<String, Dictionary> directionDictionary = new Hashtable<>();
    // Dictionary for items
    Dictionary<String, ArrayList<String>> itemsDictionary = new Hashtable<>();
    // Dictionary for interactables
    Dictionary<String, Dictionary> interactDictionary = new Hashtable<>();
    // Dictionary for item descriptions
    Dictionary<String, String> itemDescriptionDictionary = new Hashtable<>();
    // Dictionary for item descriptions
    Dictionary<String, String> coloursDictionary = new Hashtable<>();
    // Dictionary with text explanation for a new room
    Dictionary<String, String> NewRoomDictionary = new Hashtable<>();
    
    // Methods to do with new rooms
    void addRoomSequence(String room, String toPrint) {
        NewRoomDictionary.put(room, toPrint);
    }
    
    String hasRoomSequence(String room) {
        return NewRoomDictionary.get(room);
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
        coloursDictionary.put("RESET", "\u001b[38m"); // stop ansi colouring
    }
    
    String getColour(String colour) {
        return coloursDictionary.get(colour);
    }

    // Methods to do with directions
    
    void addDirection(String room, String direction, String leadsTo) {
        // Create a new direction in a room
        // Intended to be run only during program start
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
        
        System.out.println("You can move:");
        Enumeration<String> directions = roomDictionary.keys();
        while (directions.hasMoreElements()) {
            // Get dictionary key (direction)
            String roomDirection = directions.nextElement();
            // Get dictionary value (destination)
            String leadsTo = (String) directionDictionary.get(currentRoom).get(roomDirection);
            System.out.println(roomDirection + " to " + leadsTo);
        }
        System.out.println("");
    }
    
    boolean moveDir(String direction) {
        boolean success = false;
        
        String room = roomInDirection(currentRoom, direction);
        // null means no room in direction and return movement failed
        // if not null, then change the current room and return movement success
        if (room != null) {
            currentRoom = room;
            success = true;
        }
        return success;
    }
    
    // Description methods
    
    void readDescription(String room) {
        String roomDescription = descriptionDictionary.get(currentRoom);
        System.out.println("");
        System.out.println(roomDescription);
        System.out.println("");
    }
    
    void readItemDescription(String item) {
        String itemDescription = itemDescriptionDictionary.get(item);
        System.out.println("");
        if (itemDescription == null) {
            System.out.println("item description not found");
        } else {
            System.out.println(itemDescription);
        }
        System.out.println("");
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
    
    void printInteractsInRoom(String room) {
        Dictionary roomInteractables = interactDictionary.get(room);
        Enumeration <String> interactables = roomInteractables.keys();
        
        // will just say "You can interact with:" then empty if no keys in dictionary
        if (roomInteractables.size() > 0) {
            System.out.println("You can interact with:");
            while (interactables.hasMoreElements()) {
                String key = interactables.nextElement();
                System.out.println(key);
            }
            System.out.println(""); // formatting
        }
    }
    
    String[] getInteractInRoom(String room, String interactName) {
        Dictionary<String, String[]> roomInteractables = interactDictionary.get(room);
        String[] interactInfo = roomInteractables.get(interactName);
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
        addItem("Inventory", item);
    }
    
    boolean removeInventory(String item) {
        return removeItem("Inventory", item);
    }
    
    boolean hasItem(String item) {
        ArrayList<String> roomItems = itemsDictionary.get("Inventory");
        return roomItems.contains(item);
    }
    
    void getItems(String room) {
        ArrayList<String> roomItems = itemsDictionary.get(room);
        System.out.println("Items in this room:");
        
        System.out.println(roomItems);
    }
    
    void printInventory() {
        ArrayList<String> roomItems = itemsDictionary.get("Inventory");
        System.out.println("Items in inventory:");
        // prints with square brackets but this is intentional because it looks better
        System.out.println(roomItems);
    }
    
    // Instruction methods
    void waitForInput() {
        System.out.println("press enter to continue");
        keyboard.nextLine();
    }
    
    void howToPlay() {
        System.out.println("There are 6 possible directions:");
        // print each element in DIRECTIONSLIST
        for (String direction: DIRECTIONSLIST) {
            System.out.println(direction);
        }
        waitForInput();
        System.out.println("to get the description of the current room, type 'description'");
        System.out.println("for the description of an item, type the 'USE ' and items name");
        waitForInput();
        System.out.println("pick up an item with, 'pickup ' and item name");
        System.out.println("drop and item with 'drop ' and item name");
        waitForInput();
        System.out.println("to interact, type 'interact ' and item name");
        waitForInput();
        System.out.println("if you need to see the command list again, type 'help'");
        waitForInput();
    }
    
    void introduction() {
        System.out.println("You are on a space station and your crew have left");
        System.out.println("they don't tell you why but when you look out the window, a large meteorite is headed in your way");
        waitForInput();
        System.out.println("You must find a way into the control room and use the control panel");
        System.out.println("Once the panel is on, the automatic steering system will avoid the meteor");
        waitForInput();
    }
    
    // Command methods
    
    // Return result of returnCommand
    class CommandResult {
        CommandType Type;
        String Instructions;
        CommandResult(CommandType EnumCommandType, String CommandInstructions) {
            this.Type = EnumCommandType;
            this.Instructions = CommandInstructions;
        }
    }
    
    CommandResult returnCommand() {
        // Return type class so can fit two different types of variables
        String commandInstruction = "";
        CommandType commandType = null;
        boolean validInput = false;
        while (!validInput) {
            System.out.println("Input a command");
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
                System.out.println("Not a command");
            }
        }
        CommandResult returnPackage = new CommandResult(commandType, commandInstruction);
        return returnPackage;
    }
    
    /**
     * Constructor for objects of class Space_Station_Text_Adventure
     */
    public Space_Station_Text_Adventure()
    {
        // INITALISE VARIABLES
        // get the file for rooms
        File roomFolder = new File("Rooms");
        // Create a list of contained files
        File[] roomsFiles = roomFolder.listFiles();
        
        for (int fileIndex = 0; fileIndex < roomsFiles.length; fileIndex++) {
            String fileName = roomsFiles[fileIndex].getName();
            String roomName = fileName;
            // DIRECTIONS
            
            directionDictionary.put(roomName, new Hashtable<>());
            // Get the file with directions
            File directionsFile = new File("Rooms\\"+fileName+"\\directions.txt");
            Scanner readFile;
            try {
                // Safely open the file
                readFile = new Scanner(directionsFile);
            } catch (IOException error) {
                System.out.println("Could not open the file containing directions for " + roomName);
                System.out.println("file path: "+directionsFile.getAbsolutePath());
                System.out.println(error.getClass().getCanonicalName());
                error.printStackTrace();
                // stop because directions are an essential part of the program
                return;
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
            
            // END OF DIRECTIONS
            
            // DESCRIPTIONS
            
            File descriptionFile = new File("Rooms\\"+fileName+"\\description.txt");
            try {
                // Safely open the file
                readFile = new Scanner(descriptionFile);
            } catch (IOException error) {
                System.out.println("failed to load description for " + roomName);
                System.out.println(error.getClass().getCanonicalName());
                //error.printStackTrace();
                // continue because opening has failed and non essential part of text adventure
            }
            String roomDesc = "";
            while (readFile.hasNextLine()) {
                String line = readFile.nextLine();
                roomDesc+=line;
            }
            descriptionDictionary.put(roomName, roomDesc);
            
            // END OF DESCRIPTIONS
            
            // ITEMS
            
            File itemsFile = new File("Rooms\\"+fileName+"\\items.txt");
            try {
                // Safely open the file
                readFile = new Scanner(itemsFile);
            } catch (IOException error) {
                System.out.println("Could not open the file containing item for " + roomName);
                System.out.println("file path: "+itemsFile.getAbsolutePath());
                System.out.println(error.getClass().getCanonicalName());
                error.printStackTrace();
                // stop because directions are an essential part of the program
                return;
            }
            // New ArrayList to store items
            itemsDictionary.put(roomName, new ArrayList<String>());
            while (readFile.hasNextLine()) {
                // each line is an item name
                String line = readFile.nextLine();
                // add item to dictionary
                addItem(roomName, line);
            }
            
            // END OF ITEMS
            
            // Add each room to interactDictionary
            interactDictionary.put(roomName, new Hashtable<>());
        }
        // Fake room inventory
        itemsDictionary.put("Inventory", new ArrayList<String>());
        
        // INTERACTABLES
        // get the file for interactables
        File interactablesFolder = new File("Interactables");
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
                readFile = new Scanner(currentInteractable);
            } catch (IOException error) {
                System.out.println("Could not open the file containing interactable " + interactableName);
                System.out.println("file path: "+currentInteractable.getAbsolutePath());
                System.out.println(error.getClass().getCanonicalName());
                error.printStackTrace();
                // stop because directions are an essential part of the program
                return;
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
            if (enabledText != null && disabledText != null && room != null && startRoom != null && leadsTo != null && direction != null) {
                addInteract(room, interactableName, direction, startRoom, leadsTo, enabledText, disabledText);
            } else {
                System.out.println("an error occurred with an interactable");
            }
        }
        
        
        // ITEM DESCRIPTIONS
        // get the file for item descriptions
        File itemDescriptFolder = new File("ItemDescriptions");
        // Create a list of contained files
        File[] itemDescriptFiles = itemDescriptFolder.listFiles();
        //Scanner readFile;
        for (int i = 0; i < itemDescriptFiles.length; i++) {
            String itemName = itemDescriptFiles[i].getName();
            // delete .txt from name
            itemName = itemName.substring(0, itemName.length()-4);
            File currentItem = itemDescriptFiles[i];
            try {
                // Safely open the file
                readFile = new Scanner(currentItem);
            } catch (IOException error) {
                System.out.println("failed to load description for " + itemName);
                System.out.println(error.getClass().getCanonicalName());
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
        
        // NEW ROOM SEQUENCES
        // get the file for room sequences
        File roomSequenceFolder = new File("NewRoomSequence");
        // Create a list of contained files
        File[] roomSequenceFiles = roomSequenceFolder.listFiles();
        for (File roomFile: roomSequenceFiles) {
            String roomName = roomFile.getName();
            // delete .txt from name
            roomName = roomName.substring(0, roomName.length()-4);
            try {
                // Safely open the file
                readFile = new Scanner(roomFile);
            } catch (IOException error) {
                System.out.println("failed to load sequence for " + roomName);
                System.out.println(error.getClass().getCanonicalName());
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
        
        // USE ITEMS
        // get the file for room sequences
        File useItemsFolder = new File("UseItems");
        // Create a list of contained files
        File[] useItemsFiles = useItemsFolder.listFiles();
        for (File itemFile: useItemsFiles) {
            String itemName = itemFile.getName();
            // delete .txt from name
            itemName = itemName.substring(0, itemName.length()-4);
            try {
                // Safely open the file
                readFile = new Scanner(itemFile);
            } catch (IOException error) {
                System.out.println("failed to load item actions for " + itemName);
                System.out.println(error.getClass().getCanonicalName());
                //error.printStackTrace();
                // continue because opening has failed and non essential part of text adventure
                continue;
            }
            int line = 0;
            String EnumType = "";
            String toPrint = "";
            while (readFile.hasNextLine()) {
                line++;
                if (line == 1) {
                    EnumType = readFile.nextLine();
                } else {
                    // need to add newline character or it will be one big line
                    toPrint += readFile.nextLine() + "\n";
                }
            }
        }
        
        
        // INSTRUCTIONS
        if (false) {
            howToPlay();
            introduction();
        }
        
        initColours();
        
        // START MAIN GAME LOOP
        
        boolean gameComplete = false;
        
        while (!gameComplete) {
            // Separator between last action
            System.out.println("-".repeat(25));
            System.out.println("You are currently in " + currentRoom);
            System.out.println("");
            printInteractsInRoom(currentRoom);
            printDirections();
            getItems(currentRoom);
            printInventory();
            CommandResult command = returnCommand();
            CommandType commandType = command.Type; // enum
            String commandInstruction = command.Instructions; // extra instructions
            if (commandType == CommandType.DIRECTION) {
                String direction = commandInstruction;
                
                boolean success = moveDir(direction);
                if (success) {
                    System.out.println("Moving " + direction);
                } else {
                    System.out.println("No room in this direction!");
                }
                // SPECIAL CONDITION: tell the player that they need to fix control panel
                String sequence = hasRoomSequence(currentRoom);
                if (sequence != null) {
                    // exists
                    System.out.println(""); // formatting
                    System.out.println(sequence);
                    waitForInput(); 
                }
            } else if (commandType == CommandType.DESCRIPTION) {
                readDescription(currentRoom);
            } else if (commandType == CommandType.PICKUP) {
                String item = commandInstruction;
                boolean success = removeItem(currentRoom, item);
                if (success) {
                    System.out.println("The item was added to inventory!");
                    addInventory(item);
                } else {
                    System.out.println("The item does not exist!");
                }
            } else if (commandType == CommandType.DROP) {
                String item = commandInstruction;
                boolean success = removeInventory(item);
                if (success) {
                    System.out.println("The item was dropped!");
                    addItem(currentRoom, item);
                } else {
                    System.out.println("The item does not exist!");
                }
            } else if (commandType == CommandType.USE) {
                String object = commandInstruction;
                if (object.equals("potato") && hasItem("potato") && currentRoom.equals("ControlRoom")) {
                    // game complete!
                    gameComplete = true;
                } else if (object.equals("battery") && hasItem("battery") && currentRoom.equals("ControlRoom")) {
                    System.out.println("its a 0 volt battery...");
                    System.out.println("not enough charge to power the control panel");
                } else if (object.equals("jetpack") && hasItem("jetpack") && currentRoom.equals("Space")) {
                    System.out.println("you flew back to the space station!");
                    removeInventory(object);
                    currentRoom = "Entrance";
                } else if (hasItem(commandInstruction)) {
                    // holding the item but useless...
                    System.out.println("It did nothing");
                    readItemDescription(object);
                } else {
                    // Not holding this
                    System.out.println("You are not holding this...");
                }
            } else if (commandType == CommandType.INTERACT) {
                String object = commandInstruction;
                String[] info = getInteractInRoom(currentRoom, object);
                if (info == null) {
                    // not found
                    System.out.println("The object does not exist");
                } else {
                    String startingRoom = info[0];
                    String leadsTo = info[1];
                    String direction = info[2];
                    String enabledText = info[3];
                    String disabledText = info[4];
                    System.out.println(""); // formatting
                    if (roomInDirection(startingRoom, direction) != null) {
                        // already exists so revert changes
                        System.out.println(disabledText);
                        delDirection(startingRoom, direction);
                    } else {
                        System.out.println(enabledText);
                        addDirection(startingRoom, direction, leadsTo);
                    }
                    System.out.println(""); // formatting
                }
            } else if (commandType == CommandType.HELP) {
                howToPlay();
            } else {
                // Idealy would never occur but just in case
                System.out.println("an error occured");
            }
        }
        System.out.println("The potato has powered the control panel!");
        System.out.println("You successfully diverted the space station and are now safe from the meteor!");
        System.out.println("Congratulations!");
    }
}
