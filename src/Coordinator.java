import java.rmi.Naming;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Coordinator extends CoordinatorImpl {
    private static Logger logger = Logger.getLogger(Coordinator.class.getName());

    public Coordinator(ArrayList<String> serverPorts) throws RemoteException {
        super(serverPorts);
    }

    public static void main(String args[]) {
        // The port numbers of all 5 servers are required to be provided through arguments
        if (args.length < 5){
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) +" Please provide port numbers for all 5 servers.");
            return;
        }

        try {
            ArrayList<String> serverPorts = new ArrayList<String>();
            for (String arg : args) {
                serverPorts.add(arg);
            }

            CoordinatorInterface coordinatorObj = new CoordinatorImpl(serverPorts);
            // Bind this object instance to the name "coordinator".
            Naming.rebind("coordinator", coordinatorObj);
            logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + " Coordinator bound in naming");
        } catch (Exception e) {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) + " Coordinator error: " + e.getMessage());
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
