# Multi-threaded Key-Value Store using RPC & Paxos
## Project Overview:
* Server and Client use Java RMI to communicate:
    - Coordinator:
        1. `CoordinatorInterface` is the remote interface for the Coordinator
        2. `CoordinatorImpl` is the implementation of the `CoordinatorInterface`.
           It includes a list of all 5 servers to connect to. Its `paxosTwoPhase()` function performs the Paxos, including 
           Phase1-prepare, Phase2-accept, and Phase3-learn
        3. `Coordinator` extends `CoordinatorImpl`, includes the main func to create and registry the coordinator object

    - Server:
        1. `KeyValueStore` is the remote interface for the KV store server
        2. `KeyValueStoreImpl` is the implementation of the `KeyValueStore`. 
           - In its `handleClientRequest()` func, it calls initializePaxosTwoPC() func to implement Paxos algorithm.
           - I set the failure probability of each acceptor/server to be 5%.
        3. `StoreMap` is the concurrent hash map to store the key value. It handles the GET, PUT, DELETE operations.

    - Client:
        1. Client can connect to any of the 5 replicated servers by providing that server's port. The client connects
           to the server with the help of the RMI registry


## Code Testing Instruction:
Here is the instruction for Linux environment:
1. Open one terminal, run 'javac *.java' to compile
2. Registry the rmi by running command 'rmiregistry &'

3. Open 5 terminals to run the 5 replicated servers
    - Don't forget to enter the Server port when running the server
    - For example:
        - In terminal 1, start the server1 by running 'java Server 8080'
        - In terminal 2, start the server2 by running 'java Server 8081'
        - In terminal 3, start the server3 by running 'java Server 8082'
        - In terminal 4, start the server4 by running 'java Server 8083'
        - In terminal 5, start the server5 by running 'java Server 8084'
   - If you want to test restart the server with the recovered storeMap, you can pass in one more port number in the 
     arguments when running the server

4. Open another terminal to run the Coordinator
    - Don't forget to enter the 5 ports number of 5 servers
    - For example:
        - In this terminal 6, start the coordinator by running 'java Coordinator 8080 8081 8082 8083 8084'

5. Open another two or even more terminals to run clients for multi-threads
    - Don't forget to enter the port number of the server that the client wants to connect
    - For example:
        - In terminal 7, start the client1 by running 'java Client 8082'
        - In terminal 8, start the client2 by running 'java Client 8083'

6. After finishing prepopulate, users can alternately input PUT, GET, or DELETE operations in client terminals 6 & 7
   to test multi-threads

## Code Testing Result:
1. Even though the client1 only connects to the server3, for each of its requests, all replicated servers do the
   corresponding CRUD operations to the map. This is shown in client logs.
2. We can tell from the coordinator logs the Paxos algorithm process.

* Please refer to Client1_logs.pdf and Client2_logs.pdf for more test details and client logs
* Please refer to Server1_logs.pdf, Server2_logs.pdf, Server3_logs.pdf, Server4_logs.pdf, and Server5_logs.pdf for server logs
* Please refer to coordinator_logs.pdf for the coordinator logs