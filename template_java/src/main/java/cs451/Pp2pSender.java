package cs451;

import java.io.IOException;
import java.net.*;
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
        this.addresses = addresses;
        this.messageList = messageList;
        this.datagramSocket = datagramSocket;
    }

    public void stop() {
        this.stopped = true;
    }

    private void send(Message message) throws IOException {
        byte[] buffer = message.formatMessage().getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(
                buffer, buffer.length, addresses.get(message.header.getDestination())
        );
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void run() {
        while (!stopped) {
            for (Message message : messageList) {
                String element = String.format(
                        Constants.REGEX_SOURCE_DESTINATION,
                        message.header.getDestination(),
                        message.header.getMessageId()
                );
                if (!stopped && !ack.contains(element)) {
                    try {
                        send(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}