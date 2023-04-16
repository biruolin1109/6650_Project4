import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.ArithmeticException;

public class Client {
    private static Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String args[]) {
        // Requires args to include the server port
        // Here we can give the port of any one of five servers
        if (args.length < 1){
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) +" Please provide a port number for " +
                    "any of the five servers for the client to connect");
            return;
        }

        try {
            String port = args[0];
            String serverName = "KeyValueStoreServer_" + port;
            // Look up the registry for the remote object
            KeyValueStore serverObj = (KeyValueStore) Naming.lookup(serverName);

            //Call the remote method to do prepopulate using the following hardcoded commands
            String[] prepopulateCommands = new String[15];
            prepopulateCommands[0] = "PUT (1, 4)";
            prepopulateCommands[1] = "PUT (10, 12)";
            prepopulateCommands[2] = "GET (10)";
            prepopulateCommands[3] = "DELETE (12)";
            prepopulateCommands[4] = "DELETE (1)";
            prepopulateCommands[5] = "GET (1)";
            prepopulateCommands[6] = "DELETE (10)";
            prepopulateCommands[7] = "PUT (10, 30)";
            prepopulateCommands[8] = "GET (10)";
            prepopulateCommands[9] = "PUT (23, 589)";
            prepopulateCommands[10] = "DELETE (23)";
            prepopulateCommands[11] = "PUT (9, 675)";
            prepopulateCommands[12] = "GET (23)";
            prepopulateCommands[13] = "GET (9)";
            prepopulateCommands[14] = "DELETE (9)";

            for (String command: prepopulateCommands) {
                String response = serverObj.handleClientRequest(command);
                logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + " Response for prepopulate ["
                        + command + "] is [" + response + "].");
            }

            // Use the scanner to read the user commands from console for dynamic operations
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please input the use commands  for the Key-Value Store:" + '\n');
            System.out.println("The command should follow the format : PUT (key, value) or GET (key) or DELETE (key)." + '\n');

            while (scanner.hasNextLine()) {
                String userCommand = scanner.nextLine();
                String response = serverObj.handleClientRequest(userCommand);
                logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " Response for user dynamic command [" + userCommand + "] is [" + response + "].");
            }


        }
        catch (MalformedURLException murle) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "MalformedURLException: " + murle);
        }
        catch (RemoteException re) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "RemoteException: " + re);
        }
        catch (NotBoundException nbe) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "NotBoundException: " + nbe);
        }
        catch (ArithmeticException ae) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "ArithmeticException: " + ae);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Error: " + e);
            e.printStackTrace();
        }
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