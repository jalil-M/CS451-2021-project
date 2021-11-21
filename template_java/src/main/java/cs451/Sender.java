package cs451;

import java.net.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.stream.*;

class Sender extends Thread {

    private int id;
    private int msgNumber;
    private int nbMessageToSend;
    private boolean endBroadcast;
    private DatagramSocket datagramSocket;
    private static Sender singleInstance = null;
    public static Map<String, Set<Integer>> receivedMessages = new HashMap<>();

    private Sender(int id , DatagramSocket datagramSocket, int nbMessage) {
        this.id = id;
        msgNumber = 1;
        this.endBroadcast = false;
        this.nbMessageToSend = nbMessage;
        this.datagramSocket = datagramSocket;
    }

    public boolean endBroadcast(){
        return this.endBroadcast;
    }

    public static Sender getInstance(int id , DatagramSocket datagramSocket, int nbMessage)
    { 
        if (singleInstance == null) {
            singleInstance = new Sender(id, datagramSocket, nbMessage);
        }
        return singleInstance;
    }

    public static void addIdToReceive(String msg, int id){
        synchronized (receivedMessages){
            if (!receivedMessages.containsKey(msg)) {
                receivedMessages.put(msg, Set.of(id));
            }
            else{
                Set<Integer> basisMessages = receivedMessages.get(msg);
                if (!basisMessages.contains(id)) {
                    Set<Integer> updatedMessages = new HashSet<>(basisMessages);
                    updatedMessages.add(id);
                    receivedMessages.put(msg,updatedMessages);
                }
                
            }
        }

    }

    public static void addAllToReceive(String msg, int x, int y){
        Set<Integer> idStream = Stream.of(x, y).collect(Collectors.toSet());
        synchronized (receivedMessages){
            if (!receivedMessages.containsKey(msg)) {
                receivedMessages.put(msg, idStream);
            }
            else{
                Set<Integer> basisMessages = receivedMessages.get(msg);
                if (!basisMessages.equals(idStream)) {
                    Set<Integer> updatedMessages = new HashSet<>(basisMessages);
                    updatedMessages.addAll(idStream);
                    receivedMessages.put(msg,updatedMessages);
                }
            }
        }
    }

    /**
     * This method handles the sender for each process
     */

    @Override
    public void run(){
        UniformReliableBroadcast uniformReliableBroadcast = UniformReliableBroadcast.getInstance(id, datagramSocket);
        uniformReliableBroadcast.start();
        while (msgNumber <= nbMessageToSend){
            String msg = new Message(id, msgNumber).toString();
            synchronized(receivedMessages){
                receivedMessages.put(msg, Set.of(id));
            }
            Pp2pEvents.pp2pBroadcast(datagramSocket, id, msg);
            msgNumber += 1;
        }
        this.endBroadcast = true;
    }

}
        