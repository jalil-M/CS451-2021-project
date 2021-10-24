package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
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
        byte[] buffer = new byte[32768]; //32kb (more than sufficient for this submission)
        this.localProcess = localProcess;
        this.datagramSocket = datagramSocket;
        this.deliveredMessages = deliveredMessages;
        this.datagramPacket = new DatagramPacket(buffer, buffer.length);
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    private void toAck(Message message, Message ackMessage) throws IOException {
        InetSocketAddress sourceAddress = addresses.get(message.header.getSource());
        byte[] buffer = ackMessage.formatMessage().getBytes();
        datagramPacket.setSocketAddress(sourceAddress);
        datagramPacket.setData(buffer, 0, buffer.length);
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void run() {
        while(!stopped) {
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message message = new Message(new String(datagramPacket.getData(),
                    0, datagramPacket.getLength()));
            if (message.header.getMessageType().equals(Type.MSG)) {
                try {
                    String element = String.format(
                            Constants.REGEX_SOURCE_DESTINATION,
                            message.header.getSource(),
                            message.header.getMessageId()
                    );
                    deliveredMessages.putIfAbsent(element, message);
                    Message ackMessage = new Message(
                            message.header.getMessageId(),
                            Type.ACK,
                            localProcess,
                            message.header.getSource()
                    );
                    toAck(message, ackMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String element = String.format(
                        Constants.REGEX_SOURCE_DESTINATION,
                        message.header.getSource(),
                        message.header.getMessageId()
                );
                this.ack.add(element);
            }

        }
    }
}