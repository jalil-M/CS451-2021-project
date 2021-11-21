package cs451.links;

import cs451.fifo.Pp2pEvents;

import java.net.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Receiver extends Thread {

    private int id;
    public static Set<String> delivered;
    private DatagramSocket datagramSocket;
    private static Receiver singleInstance = null;
    public  Map<Integer,Set<String>> fromDelivered;
    private Map<Integer,Integer> integerMapMessageNumber;

    private Receiver(int id, DatagramSocket datagramSocket) {
        this.id = id;
        delivered = new HashSet<>();
        fromDelivered = new HashMap<>();
        this.datagramSocket = datagramSocket;
        integerMapMessageNumber = new HashMap<>();
    }

    public DatagramSocket getDatagramSocket(){
        return datagramSocket;
    }

    public int getClientId(){
        return id;
    }

    public Set<String> getDelivered(){
        return delivered;
    }

    public void addDelivered(String msg){
        delivered.add(msg);
    }

    public int getExpectedMsgNumber(int fromId){
        return integerMapMessageNumber.getOrDefault(fromId,1);
    }

    public void setExpectedMsgNumber(int fromId, int msgNumber){
        integerMapMessageNumber.put(fromId,msgNumber);
    }

    public static Receiver getInstance(int id , DatagramSocket datagramSocket) {
        if (singleInstance == null) {
            singleInstance = new Receiver(id, datagramSocket);
        }
        return singleInstance;
    }

    public  void addMsgToDelivered(int fromId, String msg){
        if (!fromDelivered.containsKey(fromId)){
            fromDelivered.put(fromId,Set.of(msg));
        }
        else{
            Set<String> updated = new HashSet<>(fromDelivered.get(fromId));
            updated.add(msg);
            fromDelivered.put(fromId,updated);
        }
    }

    /**
     * This method handles the receiver data for each process
     */

    @Override
    public void run(){
        while (true) {
            try{
                byte[] buffer = new byte[2048]; // sufficient size
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 2048);
                datagramSocket.receive(datagramPacket);
                Pp2pEvents.handleDelivery(this, datagramPacket);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}