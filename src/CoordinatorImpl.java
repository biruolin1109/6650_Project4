import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoordinatorImpl extends UnicastRemoteObject implements CoordinatorInterface {
    private static Logger logger = Logger.getLogger(CoordinatorImpl.class.getName());

    // The coordinator stores all 5 servers
    private static ArrayList<KeyValueStore> servers;

    private static ExecutorService executorService = Executors.newFixedThreadPool(15);

    // Implementations must have an explicit constructor in order to declare the RemoteException exception
    public CoordinatorImpl(ArrayList<String> serverPorts) throws RemoteException {
        super();

        this.servers = new ArrayList<>();
        try {
            // Look up all other five servers and store servers
            for (String serverPort: serverPorts) {
                String severName = "KeyValueStoreServer_" + serverPort;
                KeyValueStore serverObj = (KeyValueStore) Naming.lookup(severName);
                this.servers.add(serverObj);
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
     * This function performs the Paxos algorithm
     * In Phase 1, the proposer sends the PREPARE message to acceptors
     * In Phase 2, the proposer sends the ACCEPT message to acceptors
     * In Phase 3, the proposer sends the LEARN message to acceptors
     */
    @Override
    public synchronized boolean paxosTwoPhase(Proposal proposal) throws RemoteException {
        int half = (int) Math.ceil(servers.size() / 2);

        //Phase1: send the prepare message
        int promised = 0;
        for (KeyValueStore acceptor : servers) {
            try {
                Promise promise = acceptor.promise(proposal);
                if (promise == null) {
                    logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Sever " + acceptor.getPort() +
                            " doesn't respond to the proposal " + proposal.getOperation());
                }
                if (promise.getStatus().equals("PROMISED") || promise.getStatus().equals("ACCEPTED")) {
                    logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Sever " + acceptor.getPort() +
                            " promised the proposal " + proposal.getOperation());
                    promised += 1;
                } else {
                    logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Sever " + acceptor.getPort() +
                            " rejected the proposal " + proposal.getOperation());
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Acceptor not respond to the PREPARE message.");
                continue;
            }
        }
        if (promised < half) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Fewer than half of servers promised the proposal "
                    + proposal + ". Consensus hasn't been reached.");
            return false;
        }


        //Phase2: send the accept message
        int accepted = 0;
        for (KeyValueStore acceptor : servers) {
            try {
                Boolean isAccepted = acceptor.accept(proposal);
                if (isAccepted == null) {
                    logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Sever " + acceptor.getPort()
                            + " doesn't respond to the proposal " + proposal.getOperation());
                }
                if (isAccepted) {
                    logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Sever " + acceptor.getPort()
                            +  " accepted the proposal " + proposal.getOperation());
                    accepted += 1;
                } else {
                    logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Sever " + acceptor.getPort()
                            + " didn't accept the proposal " + proposal.getOperation());
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Acceptor not respond to the ACCEPT message.");
                continue;
            }
        }
        if (accepted < half) {
            logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Fewer than half of servers accepted the proposal "
                    + proposal + ". Consensus hasn't been reached.");
            return false;
        }

        //Phase3: send the learn message
        Boolean hasLearned = false;
        for (KeyValueStore acceptor : servers) {
            try {
                hasLearned = acceptor.learn(proposal);
            } catch(Exception e) {
                logger.log(Level.WARNING, formatTime(System.currentTimeMillis()) + "Error " + e + " occurs when the acceptor tries to respond to the LEARN message.");
                continue;
            }
        }
        return hasLearned;
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
