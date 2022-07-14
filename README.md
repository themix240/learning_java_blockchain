# Blockchain

Simple proof-of-work cryptocurrency blockchain implemented in Java divided into client and node functionality.
Client can connect to any node and start mining, make transaction or check wallet.
Nodes are connected into peer-to-peer network, and its main functionality is verifying state of blockchain and
transmitting new blocks to other nodes.

## Structure of project

Project is divided into three maven modules

- Blockchain node
- Blockchain client
- Common section (shared functionallity between both of them)

## Configuration

Configuration files exists in BlockchainNode and BlockchainClient.  
### Variables in BlockchianNode config file.
- **client_port**
  - port on which clients can connect to this node
- **blockchain_file_type**
  - file type of saved blockchain  
  - available types: txt, json
- **blockchain_path**
  - path were blockchain will be saved e.g. /User/username/blockchain
- **userDB**
  - path to file where mapping of publicKeys to username will be stored (for more friendly usage)
- **nodes_ips**
  - path to file which contains ip addresses of nodes to which our node
  will connect ip in IP:Port format e.g 127.0.0.22:6000 (also supported localhost:6000)
- **p2pPort**
  - port on which node listens to upcoming connections from other nodes

### Variables in BlockchainClient config file.
- **ip_address**
  - ip_address to node (recommended localhost)
- **node_port** 
  - port on which client will try to connect to node 
- **keys_path**
  - path to file where public and private keys of user will be stored after registration 





