package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Pp2pReceiver implements Runnable {

    private final int localProcess;
    private boolean stopped = false;
    private final DatagramPacket datagramPacket;
    private final DatagramSocket datagramSocket;
    private final ConcurrentSkipListSet<String> ack;
    private final ConcurrentHashMap<String, Message> deliveredMessages;
    private final HashMap<Integer, InetSocketAddress> addresses;

    public Pp2pReceiver(ConcurrentHashMap<String, Message> deliveredMessages,
                        ConcurrentSkipListSet<String> ack,
                        int localProcess, DatagramSocket datagramSocket,
                        HashMap<Integer, InetSocketAddress> addresses) {
        this.ack = ack;
        this.addresses = addresses;
        byte[] buffer = new byte[32768]; //32kb
        this.localProcess = localProcess;
        this.datagramSocket = datagramSocket;
        this.deliveredMessages = deliveredMessages;
        this.datagramPacket = new DatagramPacket(buffer, buffer.length);
    }

    private void processMessage(Message message) throws IOException {
        Header header = message.header;
        String element = String.format(
                "%d:%d",
                header.getSource(),
                header.getMessageId()
        );
        deliveredMessages.putIfAbsent(element, message);
        toAck(message);
    }

    private void toAck(Message message) throws IOException {

        Header header = message.header;
        Message ackMessage = new Message(
                header.getMessageId(),
                Type.ACK,
                localProcess,
                header.getSource()
        );

        int sourceProcess = message.header.getSource();
        InetSocketAddress sourceAddress = addresses.get(sourceProcess);

        byte[] buffer = ackMessage.serialize().getBytes(StandardCharsets.UTF_8);
        datagramPacket.setSocketAddress(sourceAddress);
        datagramPacket.setData(buffer, 0, buffer.length);
        datagramSocket.send(datagramPacket);
    }

    private void processACK(Message ack) {
        Header header = ack.header;

        String element = String.format(
                "%d:%d",
                header.getSource(),
                header.getMessageId()
        );

        this.ack.add(element);
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {
        while(!stopped) {
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String raw = new String(datagramPacket.getData(),
                    0, datagramPacket.getLength(),
                    StandardCharsets.UTF_8);

            Message message = new Message(raw);

            if (message.isMSG()) {
                try {
                    processMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                processACK(message);
            }

        }
    }
}