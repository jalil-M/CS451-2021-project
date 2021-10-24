package cs451;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class Pp2pSender implements Runnable {

    private boolean stopped = false;
    private final List<Message> messageList;
    private final DatagramSocket datagramSocket;
    private final ConcurrentSkipListSet<String> ack;
    private final HashMap<Integer, InetSocketAddress> addresses;

    public Pp2pSender(List<Message> messageList,
                      ConcurrentSkipListSet<String> ack,
                      DatagramSocket datagramSocket,
                      HashMap<Integer, InetSocketAddress> addresses) {
        this.ack = ack;
        this.messageList = messageList;
        this.addresses = addresses;
        this.datagramSocket = datagramSocket;
    }

    private void send(Message message) throws IOException {

        int destinationProcess = message.header.getDestination();
        InetSocketAddress destinationAddress = addresses.get(destinationProcess);
        byte[] buffer = message.serialize().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, destinationAddress
        );
        datagramSocket.send(packet);

    }

    private boolean checkACK(Message message) {
        Header header = message.header;
        String element = String.format(
                "%d:%d",
                header.getDestination(),
                header.getMessageId()
        );
        return ack.contains(element);
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            for (Message forSend : messageList) {
                if (!stopped && !checkACK(forSend)) {
                    try {
                        send(forSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}