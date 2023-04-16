import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreMap {
    private static Logger logger = Logger.getLogger(StoreMap.class.getName());
    public static ConcurrentHashMap<Integer, Integer> map;

    public StoreMap() {
        map = new ConcurrentHashMap<Integer, Integer>();
    }

    // PUT is to update or insert
    public static boolean put(ArrayList<String> userCommands) {
        int key = Integer.parseInt(userCommands.get(1));
        int value = Integer.parseInt(userCommands.get(2));

        try{
            //insert new key pair value
            if (!map.containsKey(key)) {
                logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Successfully INSERT (" + key + ", " + value
                        + ") pair by PUT command");
            }
            //update existing key pair value
            else { //update
                logger.log(Level.INFO, formatTime(System.currentTimeMillis()) +  "Successfully UPDATE (" + key + ", " + value
                        + ") pair by PUT command");
            }
            map.put(key, value);
            toMapString();
            return true;
        }catch (Exception e) {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Failed to PUT (" + key + ", " + value +
                    ") pair by PUT command");
        }
        return false;
    }

    public static boolean contain(ArrayList<String> userCommands) {
        int key = Integer.parseInt(userCommands.get(1));
        if (map.containsKey(key)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean delete(ArrayList<String> userCommands) {
        int key = Integer.parseInt(userCommands.get(1));
        if (contain(userCommands)) {
            int removedValue = map.remove(key);
            logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Successfully DELETE (" + key + ", " + removedValue + ") pair");
            toMapString();
            return true;
        } else {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Failed to DELETE (" + key + ")");
            return false;
        }
    }

    public static String get(ArrayList<String> userCommands) {
        String response = "";
        int key = Integer.parseInt(userCommands.get(1));
        if (map.containsKey(key)) {
            int value = map.get(key);
            response += "Successfully GET " + value + " for " + key;
        } else {
            response += "Key " + key + " doesn't exist, cannot GET " + key;
        }
        toMapString();
        return response;
    }

    /**
     * This function is a helper function to log the key value pairs in the store map
     */
    public static String toMapString() {
        String result = "";
        for (int key: map.keySet()) {
            result += "(key: " + key  + ", value: " + map.get(key) + ") ";
        }
        logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " The current Key-Value Store is: " + result);
        return result;
    }

    /**
     * This function is a helper function to make a copy to the current storeMap
     */
    public static StoreMap copyStoreMap(){
        StoreMap copiedStoreMap = new StoreMap();
        for (Map.Entry<Integer, Integer> entry: map.entrySet()){
            copiedStoreMap.map.put(entry.getKey(), entry.getValue());
        }
        return copiedStoreMap;
    }

    /**
     * This function formats the current time in milliseconds to a human-readable format
     */
    private static String formatTime(long currentTime){
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
        String formattedTime = sdf.format(currentTime);
        return formattedTime;
    }

}
