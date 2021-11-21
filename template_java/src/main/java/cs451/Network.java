package cs451;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.*;  
import java.io.IOException;

public class Network {

    private int myId;
    private int nbMessage;
    private List<Host> hostList;
    private boolean endBroadcast;
    private static String outputPath;
    public static int networkMajority;
    private static Network singleInstance = null;
    protected static Map<Integer, SocketAddress> socketAddressHashMap =  new HashMap<>();
    

    private Network(List<Host> hostList, int myId, int nbMessage, String outputPath){
        this.myId = myId;
        this.hostList = hostList;
        this.endBroadcast = false;
        this.nbMessage = nbMessage;
        this.outputPath = outputPath;
        this.networkMajority = (int) Math.ceil(this.hostList.size()/2.0);
    }

    private void initHostSocket(){
        try{
            for (Host host: hostList) {
                SocketAddress socket = new InetSocketAddress(InetAddress.getByName(host.getIp()), host.getPort());
                socketAddressHashMap.put(host.getId(), socket);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Network getInstance(List<Host> hostList, int myId, int nbMessage, String outputPath)
    { 
        if (singleInstance == null) {
            singleInstance = new Network(hostList, myId, nbMessage, outputPath);
        }
        return singleInstance;
    } 

    public boolean endBroadcasting(){
        return endBroadcast;
    }

    public static String getOutputPath(){
        return outputPath;
    }

    private static void sleep() {
        try{
            Thread.sleep(10);
        }
        catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    /**
     * This methods handles the network view of the current process (sender, receiver, perfect failure detector)
     */
    public void execute(){
        initHostSocket();
        DatagramSocket mySocket;
        try{
            mySocket = new DatagramSocket(socketAddressHashMap.get(myId));
            System.out.println("Active socket for process : " + myId);
            System.out.println("Network majority : "+ networkMajority);
            Sender sender = Sender.getInstance(myId, mySocket, nbMessage);
            Receiver receiver = Receiver.getInstance(myId,mySocket);
            receiver.start();
            sender.start();
            while (!sender.endBroadcast()){
                sleep();
            }
            this.endBroadcast = true;
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

}