package cs451.fifo;

import cs451.Constants;
import cs451.links.Receiver;
import cs451.links.Sender;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class FifoBroadcast extends Thread {

    private int myId;
    private int timeout;
    private DatagramSocket datagramSocket;
    private static FifoBroadcast singleInstance = null;

    private FifoBroadcast(int myId, DatagramSocket datagramSocket) {
        this.myId = myId;
        this.timeout = 100;
        this.datagramSocket = datagramSocket;
    }

    public static FifoBroadcast getInstance(int myId, DatagramSocket datagramSocket) {
        if (singleInstance == null)
            singleInstance = new FifoBroadcast(myId, datagramSocket);
        return singleInstance;
    }

    /**
     * This method helps us ensure that all messages are received using the ACK system
     */

    @Override
    public void run() {
        Object object = new Object();
        try {
            synchronized (object) {
                while (true) {
                    Set<String> toRemove = new HashSet<>();
                    synchronized (Sender.receivedMessages) {
                        Sender.receivedMessages.entrySet().parallelStream().forEach(entry -> {
                            String msgKey = entry.getKey();
                            Set<Integer> unreceived_ack = new HashSet<>(Network.socketAddressHashMap.keySet());
                            unreceived_ack.removeAll(entry.getValue());
                            if (!unreceived_ack.isEmpty() && unreceived_ack.size() >= Network.networkMajority) {
                                unreceived_ack.forEach(id -> Pp2pEvents.pp2pSendMessage(datagramSocket, id, msgKey));
                            }
                            else {
                                toRemove.add(msgKey);
                            }
                        });
                        Receiver receiver = Receiver.getInstance(myId, datagramSocket);
                        Map<Integer, List<String>> messageById = toRemove.stream()
                                .collect(Collectors.groupingBy(s -> Integer.parseInt(s.split(Constants.DELIMITER)[0])));
                        List<String> toDeliver = new ArrayList<>();
                        messageById.entrySet().parallelStream().forEach(entry -> {
                            List<String> entryValue = entry.getValue();
                            Collections.sort(entryValue);
                            int currentId = entry.getKey();
                            int msgNumber = receiver.getExpectedMsgNumber(currentId);
                            for (String message : entryValue){
                                if (msgNumber == Integer.parseInt(message.split(Constants.MSG)[1])) {
                                    toDeliver.add(message);
                                    msgNumber += 1;
                                } else {
                                    toRemove.remove(message);
                                }
                            }
                            receiver.setExpectedMsgNumber(currentId, msgNumber);
                        });
                        toDeliver.stream().sorted().parallel()
                                .forEachOrdered(message -> {
                                    try {
                                        Pp2pEvents.writeAndDeliver(receiver, message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                        Sender.receivedMessages.keySet().removeAll(toRemove);
                    }
                    object.wait(timeout);
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}