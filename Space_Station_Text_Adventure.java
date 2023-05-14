/**
 * Text adventure where you have to divert the space station from collision course
 * with a meteor
 *
 * @author Ritesh Ravji
 * @version 4/5/23
 */

import java.util.Scanner; // Read keyboard
import java.util.Arrays; // Possible
import java.io.File; // Get files
import java.io.IOException; // Handle file exceptions

import java.util.Dictionary; // Save room data in dictionary
import java.util.Hashtable; // Goes with Dictionary
import java.util.Enumeration; // Goes with Dictionary

public class Space_Station_Text_Adventure
{
    // Create list which contains list of possible directions to check whether a direction is possible
    String[] DIRECTIONSLIST = {"north", "south", "east", "west", "up", "down"};
    String currentRoom = "Entrance"; // Starting room
    
    // Create a new dictionary to store descriptions
    Dictionary<String, String> descriptionDictionary = new Hashtable<>();
    // Dictionary for directions
    Dictionary<String, Dictionary> directionDictionary = new Hashtable<>();
    // Dictionary for items
    Dictionary<String, Dictionary> itemsDictionary = new Hashtable<>();
    
    // Methods to do with directions
    
    void addDirection(String room, String direction, String leadsTo) {
        // Create a new direction in a room
        // Intended to be run only during program start
        directionDictionary.get(room).put(direction, leadsTo);
    }
    
    String roomInDirection(String room, String direction) {
        // Check if room is in a direction and print room name or return null
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
    
    /**
     * Constructor for objects of class Space_Station_Text_Adventure
     */
    public Space_Station_Text_Adventure()
    {
        // get the file for rooms
        File roomFolder = new File("Rooms");
        // Create a list of contained files
        File[] roomsFiles = roomFolder.listFiles();
        
        // Set room names length as amount of files - probably not needed
        String[] ROOMNAMES = new String[roomsFiles.length];
        for (int fileIndex = 0; fileIndex < roomsFiles.length; fileIndex++) {
            String fileName = roomsFiles[fileIndex].getName();
            String roomName = fileName;
            ROOMNAMES[fileIndex] = roomName;
            
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
                    String desitination = splitLine[1];
                    System.out.println("Adding" + direction + startingRoom);
                    addDirection(startingRoom, direction, desitination);
                }
            }
        }
    }
}
