package cs451;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class Pp2pSender implements Runnable {

    private final List<Message> messageList;
    private boolean stoppingCriterion = false;
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

    public void setStoppingCriterion(boolean stoppingCriterion) {
        this.stoppingCriterion = stoppingCriterion;
    }

    // sending process with java net
    private void toMessage(Message message) throws IOException {
        byte[] buffer = message.formatMessage().getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(
                buffer, buffer.length, addresses.get(message.header.getDestination())
        );
        datagramSocket.send(datagramPacket);
    }

    /**
     * This method executes the Perfect P2P sender (sends message if ACK is not sent by receiver, respects the "no duplication" property for perfectness)
     */
    @Override
    public void run() {
        while (!stoppingCriterion) {
            for (Message message : messageList) {
                String element = String.format(
                        Constants.REGEX_SOURCE_DESTINATION,
                        message.header.getDestination(),
                        message.header.getMessageId()
                );
                if (!stoppingCriterion && !ack.contains(element)) {
                    try {
                        toMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}