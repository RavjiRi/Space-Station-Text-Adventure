/**
 * Text adventure where you have to divert the space station from collision course
 * with a meteor
 *
 * @author Ritesh Ravji
 * @version 4/5/23
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
        USE
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
    
    // Interact methods
    
    void addInteract(String room, String interactName, String direction, String startRoom, String leadsTo, String enabledText, String disabledText) {
        // method adds item to room
        
        // Get all current items in room
        Dictionary<String, String[]> roomInteractables= interactDictionary.get(room);
        //roomInteractables.put(new Hashtable<>());
        
        String[] interactInfo = {startRoom, leadsTo, direction, enabledText, disabledText};
        roomInteractables.put(interactName, interactInfo);
        // don't need to set item array to dictionary as it is already changed when added item
    }
    
    void printInteractsInRoom(String room) {
        Dictionary roomInteractables = interactDictionary.get(room);
        Enumeration <String> interactables = roomInteractables.keys();
        
        System.out.println("You can interact with:");
        
        while (interactables.hasMoreElements()) {
            String key = interactables.nextElement();
            System.out.println(key);
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
        
        System.out.println(roomItems);
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
            System.out.println("Input a direction");
            String userInput = keyboard.nextLine();
            
            // Checks if input is a direction
            // if user types "north" move north, not "move north"
            boolean isDirection = Arrays.asList(DIRECTIONSLIST).contains(userInput.toLowerCase());
            if (isDirection) {
                commandInstruction = userInput.toLowerCase(); 
                commandType = CommandType.DIRECTION;
                //commandType = "direction";
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
            
            // Checks if command is to pick up
            
            // Get minimum of string length or 11 otherwise getting
            // first 11 characters will error if length < 11
            int minimumChars = Math.min(userInput.length(), 11);
            String startingChars = userInput.toUpperCase().substring(0, minimumChars);
            
            if (startingChars.equals("DESCRIPTION")) {
                commandType = CommandType.DESCRIPTION;
                validInput = true;
                continue;
            }
            
            String referenceString = "PICKUP";
            minimumChars = Math.min(userInput.length(), referenceString.length());
            startingChars = userInput.toUpperCase().substring(0, minimumChars);
            if (startingChars.equals(referenceString)) {
                // Make sure length is at least "PICKUP ".length();
                if (userInput.length() >= referenceString.length()+1) {
                    commandType = CommandType.PICKUP;
                    String item = userInput.substring(referenceString.length()+1); // length of "PICKUP "
                    commandInstruction = item; 
                    validInput = true;
                    continue;
                } else {
                    System.out.println("You cannot pickup nothing!");
                    continue;
                }  
            }
            
            referenceString = "DROP";
            minimumChars = Math.min(userInput.length(), referenceString.length());
            startingChars = userInput.toUpperCase().substring(0, minimumChars);
            if (startingChars.equals(referenceString)) {
                // Make sure length is at least "DROP ".length();
                if (userInput.length() >= referenceString.length()+1) {
                    commandType = CommandType.DROP;
                    String item = userInput.substring(referenceString.length()+1); // length of "DROP "
                    commandInstruction = item; 
                    validInput = true;
                    continue;
                } else {
                    System.out.println("You cannot drop nothing!");
                    continue;
                }  
            }
            
            referenceString = "USE";
            minimumChars = Math.min(userInput.length(), referenceString.length());
            startingChars = userInput.toUpperCase().substring(0, minimumChars);
            if (startingChars.equals(referenceString)) {
                // Make sure length is at least "USE ".length();
                if (userInput.length() >= referenceString.length()+1) {
                    commandType = CommandType.USE;
                    String item = userInput.substring(referenceString.length()+1); // length of "DROP "
                    commandInstruction = item; 
                    validInput = true;
                    continue;
                } else {
                    System.out.println("You cannot use nothing!");
                    continue;
                }  
            }
            
            referenceString = "INTERACT";
            minimumChars = Math.min(userInput.length(), referenceString.length());
            startingChars = userInput.toUpperCase().substring(0, minimumChars);
            if (startingChars.equals(referenceString)) {
                // Make sure length is at least "INTERACT ".length();
                if (userInput.length() >= referenceString.length()+1) {
                    commandType = CommandType.INTERACT;
                    String item = userInput.substring(referenceString.length()+1); // length of "DROP "
                    commandInstruction = item; 
                    validInput = true;
                    continue;
                } else {
                    System.out.println("You cannot interact with nothing!");
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
                error.printStackTrace();
                // go to next file because opening has failed
                continue;
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
                    //System.out.println("Adding" + direction + startingRoom);
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
                error.printStackTrace();
                // go to next file because opening has failed
                continue;
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
                error.printStackTrace();
                // go to next file because opening has failed
                continue;
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
        // get the file for rooms
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
                error.printStackTrace();
                // go to next file because opening has failed
                continue;
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
                } else if (object.equals("picture frame") && hasItem("picture frame")) {
                    System.out.println("there is a picture of a space shuttle launch");
                    System.out.println("this spaceship was launched in 1977 and contained the voyager 1 probe which flew past saturn in 1980");
                } else if (hasItem(commandInstruction)) {
                    // holding the item but useless...
                    System.out.println("It did nothing");
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
            } else {
                // Idealy would never occur but just in case
                System.out.println("an error occured");
            }
        }
        System.out.println("The potato has powered the control panel!");
        System.out.println("You successfully diverted the space station and are now safe from the meteor!");
        System.out.println("Yay!");
    }
}
