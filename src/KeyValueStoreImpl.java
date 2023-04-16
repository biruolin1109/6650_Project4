import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Implementing the remote interface
public class KeyValueStoreImpl extends UnicastRemoteObject implements KeyValueStore {
    private static Logger logger = Logger.getLogger(KeyValueStoreImpl.class.getName());

    //Every server should have a storeMap
    private StoreMap storeMap = new StoreMap();

    // Every server should connect to coordinator for 2PC
    private static CoordinatorInterface coordinator;

    private static int proposalId = 1;
    private long maxProposalId;
    private Proposal accepted;
    private int port;

    /**
     * Implementation must have an explicit constructor in order to declare the RemoteException exception
     */
    public KeyValueStoreImpl() throws RemoteException {
        super();
        this.maxProposalId = 0;
        this.accepted = null;
    }


    /**
     * This function handles the client requests and do corresponding CRUD operations
     */
    public String handleClientRequest(String inputMessage) throws RemoteException {
        String response = "";

        // Check if the input command message follows correct pattern,
        // Report a malformedRequestException and return if not.
        ArrayList<String> userCommands = parseUserInput(inputMessage);
        if (userCommands.isEmpty()) {
            return response;
        }

        // Take CRUD operations to storeMap and calculate the response message
        String operation = userCommands.get(0);
        if (operation.equals("PUT")) {
            response = initializePaxosTwoPC(inputMessage);
        }
        else if (operation.equals("DELETE")) {
            int key = Integer.parseInt(userCommands.get(1));
            if (!storeMap.contain(userCommands)) {
                response += "Key " + key + " doesn't exist, cannot DELETE "  + key;
            } else {
                response = initializePaxosTwoPC(inputMessage);
            }

        } else if (operation.equals("GET")) {
            response = storeMap.get(userCommands);
        }

        if (response.equals("")) {
            // Report the malformed datagram packet to server log
            malformedRequestException(inputMessage);
        }
        return response;
    }


    /**
     * This function initialize Paxos Algorithm for PUT and DELETE operations.
     */
    public String initializePaxosTwoPC(String userInput) throws RemoteException {
        // Look up the coordinator and connect to the coordinator
        try {
            CoordinatorInterface coordinatorObj = (CoordinatorInterface) Naming.lookup("coordinator");
            this.coordinator = coordinatorObj;
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Proposal proposal = Proposal.generateProposal(userInput);
        boolean paxosResult = this.coordinator.paxosTwoPhase(proposal);
        if (paxosResult == true) {
            return "Successfully " + userInput;
        }
        return "Failed to " + userInput;
    }

    /**
     * Phase 1: the acceptor receives a PREPARE message
     */
    public Promise promise(Proposal proposal) throws RemoteException {
        logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " The server received a PREPARE message: " + proposal.getId()
                + "_" + proposal.getOperation());

        //Set the failure probability to 5%
        if (Math.random() <= 0.05){
            logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " The server failed randomly.");
            return null;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FutureTask<Promise> future = new FutureTask<>(new Callable<Promise>() {
            @Override
            public Promise call() throws Exception{
                if (proposal.getId() > maxProposalId){
                    // Save the highest proposal Id it has seen so far
                    maxProposalId = proposal.getId();

                    // Check if a proposal has already been accepted
                    if (accepted != null){
                        return new Promise("ACCEPTED", accepted);
                    }else{
                        return new Promise("PROMISED", proposal);
                    }

                }else{
                    return new Promise("REJECTED", null);
                }
            }
        });

        try{
            executorService.submit(future);
            Promise promiseAck = future.get(10, TimeUnit.SECONDS);
            return promiseAck;
        }  catch (InterruptedException e) {
            logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Interrupted Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Exception Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Time Out for the server response" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Phase 2: the acceptor receives an ACCEPT message
     */
    public Boolean accept(Proposal proposal) throws RemoteException{
        logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " The server received an ACCEPT message: " + proposal.getId()
                + "_" + proposal.getOperation());

        //Set the failure probability to 5%
        if (Math.random() <= 0.05){
            logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " The server failed randomly.");
            return null;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FutureTask<Boolean> future = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception{
                // Check if the proposal id is the largest Id it has ever seen
                if (proposal.getId() == maxProposalId){
                    // Save the accepted proposal
                    if (accepted == null) {
                        accepted = proposal;
                    }else{
                        accepted.setId(proposal.getId());
                        accepted.setOperation(proposal.getOperation());
                    }
                    return true;
                }else{
                    return false;
                }
            }
        });

        try{
            executorService.submit(future);
            Boolean acceptAck = future.get(10, TimeUnit.SECONDS);
            return acceptAck;
        }  catch (InterruptedException e) {
            logger.log(Level.INFO, formatTime(System.currentTimeMillis()) + "Interrupted Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Exception Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Time Out for the server response" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Phase 3: learn phase
     */
    @Override
    public Boolean learn(Proposal proposal) throws RemoteException{
        logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + " The server received a LEARN message: " + proposal.getId()
                + "_" + proposal.getOperation());
        String userInput = proposal.getOperation();
        ArrayList<String> userCommands = parseUserInput(userInput);
        String operation = userCommands.get(0);
        if (operation.equals("PUT")) {
            boolean putResult =  storeMap.put(userCommands);
            return putResult;
        }
        else if (operation.equals("DELETE")){
            boolean deleteResult =  storeMap.delete(userCommands);
            return deleteResult;
        }
        return false;
    }

    /**
     * THis function helps the restarted server to have a copied storeMap if any failure occurs
     */
    @Override
    public void recover(int peerPort) throws RemoteException{
        try{
            String serverName = "KeyValueStoreServer_" + peerPort;
            // Look up the registry for the remote object
            KeyValueStore peerServer = (KeyValueStore)Naming.lookup(serverName);
            this.storeMap = peerServer.getStoreMap().copyStoreMap();
            logger.log(Level.INFO,formatTime(System.currentTimeMillis()) + "Recover the server at the port " + peerPort);
        }catch (Exception e) {
            logger.log(Level.WARNING,formatTime(System.currentTimeMillis()) + "Failed to recover the server at the port " + peerPort);
        }

    }

    @Override
    public StoreMap getStoreMap() throws RemoteException{
        return storeMap;
    }

    @Override
    public int getPort() throws RemoteException{
        return port;
    }

    @Override
    public void setPort(int port) throws RemoteException{
        this.port = port;
    }

    /**
     * This function uses Regexp to help check if the user command follows correct pattern,
     * meanwhile it parses the command to an array list.
     * For example, for a "PUT (2, 3)" command, we will first check it follows the "PUT (int, int)" format,
     * then parse this command to an array list ["PUT", "2", "3].
     *
     * If the command doesn't follow the correct pattern, we know the server receives a malformed datagram packet,
     * report error to the server log.
     */
    private static ArrayList<String> parseUserInput(String input) {
        ArrayList<String> res = new ArrayList<String>();
        // The pattern we want commands to follow
        String pattern = "(PUT|GET|DELETE)\\s*\\((\\d+),?\\s*(\\d+)?\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);
        if (m.matches()) {
            int matchCount = m.groupCount();
            for (int i = 1; i <= matchCount; i++) {
                if (m.group(i) != null) {
                    res.add(m.group(i));
                }
            }
        }
        else {
            malformedRequestException(input);
        }
        return res;
    }

    /**
     * This function reports the malformed request error to the logger server
     */
    private static void malformedRequestException(String input) {
        logger.log(Level.SEVERE, formatTime(System.currentTimeMillis()) +  " Received malformed request " + input +
                " of length " + input.length() + " from the client");
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
