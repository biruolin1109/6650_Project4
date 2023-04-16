import java.rmi.Remote;
import java.rmi.RemoteException;

// This is the remote interface for the key value store server
public interface KeyValueStore extends Remote {
    String handleClientRequest(String inputMessage) throws RemoteException;
    Promise promise(Proposal proposal) throws RemoteException;
    Boolean accept(Proposal proposal) throws RemoteException;
    Boolean learn(Proposal proposal) throws RemoteException;
    void recover(int peerPortNumber) throws RemoteException;
    void setPort(int portNumber) throws RemoteException;
    int getPort() throws RemoteException;
    StoreMap getStoreMap() throws RemoteException;
}