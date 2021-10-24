package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class PerfectLink {

    private String output;
    private final int processId;
    private final Pp2pSender pp2pSender;
    private final Pp2pReceiver pp2pReceiver;
    private DatagramSocket datagramSocket;
    private final List<Message> messageList;
    private ConcurrentSkipListSet<String> ack;
    private final AtomicInteger atomicInteger;
    private HashMap<Integer, InetSocketAddress> addresses;
    private final ConcurrentHashMap<String, Message> deliveredMessages;

    public PerfectLink(int processId, HashMap<Integer, InetSocketAddress> addresses) throws SocketException {
        this.processId = processId;
        this.addresses = addresses;
        this.messageList = new ArrayList<>();
        this.atomicInteger = new AtomicInteger();
        this.ack = new ConcurrentSkipListSet<>();
        this.deliveredMessages = new ConcurrentHashMap<>();
        int port = this.addresses.get(processId).getPort();
        this.datagramSocket = new DatagramSocket(port);
        this.pp2pSender = new Pp2pSender(messageList, ack, datagramSocket, addresses);
        this.pp2pReceiver = new Pp2pReceiver(deliveredMessages, ack, processId, datagramSocket, addresses);
    }

    public PerfectLink(int processId, HashMap<Integer, InetSocketAddress> addresses, String output) throws SocketException {
        this(processId, addresses);
        this.output = output;
    }

    public int getProcessId() {
        return processId;
    }

    public void start() {
        new Thread(this.pp2pSender).start();
        new Thread(this.pp2pReceiver).start();
        System.out.printf("Perfect Link for Process %d \n", processId);
    }

    public void send(String payload, int destination) {
        int increment = atomicInteger.getAndIncrement();
        Message message = new Message(increment, Type.MSG, processId, destination, payload);
        messageList.add(message);
    }

    public void stop() {
        this.pp2pSender.stop();
        this.pp2pReceiver.stop();
    }

    public void saveFile() {
        try {
            FileWriter fw = new FileWriter(output);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Message data: messageList) {
                bw.write(data.broadcast() + "\n");
            }
            for (Message data: deliveredMessages.values()) {
                bw.write(data.deliver() + "\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}