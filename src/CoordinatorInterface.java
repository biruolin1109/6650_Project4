import java.rmi.Remote;
import java.rmi.RemoteException;

// This is the remote interface for the coordinator
public interface CoordinatorInterface extends Remote{
    boolean paxosTwoPhase(Proposal proposal) throws RemoteException;

}