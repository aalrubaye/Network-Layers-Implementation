# Network-Layers-Implementation

##Instructions
- The program is written using JAVA.
- Has two classes: 
    * One is (Main class): which includes the threads
  	* Second is (Node) which is the implementation of layer's 2,3,4 functionality in each node (thread)
- There is a text file (data.txt) which has to be send to the receiver
- Run the program 
- Enter the IP or the host name of the sender and the receiver
- The output will be easy enough to understand:
  * The Ethernet frames for each sending operation, consisting of all fields (even the TCP/IP header) except the payload.
- At the end, you will see another text file which its name is the same as the receiver address
  * This means that the data has been sent correctly to the destination.
  
  ##The output:
  ![output-sample-abduljaleel-al-rubaye](https://cloud.githubusercontent.com/assets/17988691/21748535/5eac43d0-d555-11e6-8a6a-eb3e7bb945f2.jpg)
  
##The Sending process
1- The method transport():
   * calls fragmentation(). This function is responsible for breaking the message into segments.
   * calls TCP_IP_header() which is creating a TCP/IP header for each one of the segments. In this function, the checksum of the header will be calculated using the function TCP_IP_header_checksum().
   * Send segments to the next layer - network().

2- The method network():
   * uses the function routing() to retrieve the value of the next hop. This function is created based on the network’s topology.
   * gives the value of the next hop to datalink()

3- The method datalink():
   * constructs an Ethernet frame for each one of the segments and add the value of the destination’s Mac based on the next hop value. Then calculates the CRC for the frame
   * send all of the frames to the next hop
   
##The Receiving process
1- The method datalink()
* checks the CRC and the destination’s Mac address whether they are correct or not.
* sends the frames up to the method network().

2- The method network()
* using the TCP_IP_header, will check the destination’s IP address whether is equal to its address or not.
* if the destination’s IP is the same as its address, will send the frames up to the method transport
* if not, will redirect the frames based on the next hop value.

3- The method transport()
* receives the segments and reassembles them.

##Network Topology
The topology of the network is defined in the function [routing()]. This function takes two values: the node that is sending and the node that is receiving data. For each one of the nodes (A,B,C, and the Router) there is a routing table defined in the function which returns the value of the next hop based on the sending node toward the receiving node. The IP numbers that are used in this topology are: A (10.10.20.1), B(192.168.25.20), and C(192.168.25.15).

##Other function
- combine_segments(): reassembles the data
- ip(): returns the ip address for the current node
- mac(): returns the mac address for the current node
- checksum(): calculates the checksum of the TCP/IP header
- CRC(): calculates the CRC32 value for each Ethernet frame

##The algorithm
####Main function()
- Create 4 treads based on the class Node
- Run all threads

####The class (NODE)
- Check the thread’s name
If it is not the current node keep it waiting
Else if it is the sending process do to transport()
Else go to datalink()

- transport()
if it’s the sending process:
fragment the data into segments
create a TCP/IP header
send to network()
if it’s the receiving process:
reassemble the segments and save them to a file

- network()
if it’s the sending process:
using routing(), retrieve the address of the next hop
if it’s the receiving process
check the IP address exists inside the TCP/IP header
if the IP is equal to current node’s ip send to transport()
else send to datalink()

- datalink()
if it’s the sending process:
create the Ethernet frames and put the dest Mac and the src Mac addresses
calculate the CRC and attach it to the frame
send the frame to the next hop
if it’s the receiving process:
receive the frames
check the dest Mac and the CRC
if they are okay send the frames to network()
