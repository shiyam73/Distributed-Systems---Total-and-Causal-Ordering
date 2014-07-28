Distributed-Systems---Total-and-Causal-Ordering
===============================================

The app multicasts every user-entered message to all app instances (including the one that is sending the message). In the rest of the description, “multicast” always means sending a message to all app instances.
App uses B-multicast. It does not implement R-multicast.
You need to come up with an algorithm that provides total-causal ordering.
A content provider is implemented using SQLite on Android to store pair.

We have fixed the ports & sockets.

App opens one server socket that listens on 10000.
We will use up to 5 AVDs. The redirection ports are 11108, 11112, 11116, 11120, 11124. All three ports are hard coded.
Every message is stored in the provider individually by all app instances. Each message is stored as a pair. The key should be the final delivery sequence number for the message (as a string); the value should be the actual message (again, as a string). The delivery sequence number should start from 0 and increase by 1 for each message.
