import java.io.IOException;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends KeyValueStoreImpl {
    private static Logger logger = Logger.getLogger(Server.class.getName());

    // The explicit constructor to declare the RemoteException exception
    public Server() throws RemoteException {
        super();
    }

    public static void main(String args[]) throws IOException {
        // Requires args to at least include the server port
        if (args.length < 1){
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) +" Please provide port number for the server.");
            return;
        }

        try {
            String port = args[0];
            // Create an object of the Server class.
            KeyValueStore server = new KeyValueStoreImpl();
            server.setPort(Integer.parseInt(port));

            // If wants to restart
            if (args.length == 2){
                String peerPort = args[1];
                server.recover(Integer.parseInt(peerPort));
            }

            // Bind this object instance to the server's unique name.
            String serverName = "KeyValueStoreServer_" + port;
            Naming.rebind(serverName, server);
            logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + " Server " + serverName + " bound in naming");
        }
        catch (Exception e) {
            logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + " Server error: " + e.getMessage());
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
