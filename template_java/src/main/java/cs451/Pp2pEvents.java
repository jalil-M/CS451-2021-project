package cs451;

import java.net.*;
import java.io.IOException;
import java.util.Map;

class Pp2pEvents {

    private Pp2pEvents() {
        throw new IllegalStateException("Events class");
    }

    // no duplication
    public static void writeAndDeliver(Receiver receiver, String data) throws IOException {
        if (!receiver.getDelivered().contains(data)) {
            String[] msg = data.split(Constants.DELIMITER);
            int id = Integer.parseInt(msg[0]);
            receiver.addDelivered(data);
            receiver.addMsgToDelivered(id, data);
            Utils.writeFiles(Network.getOutputPath(), "d " + id + " " + data.split(Constants.MSG)[1] + "\n");
           
        }
    }

    public static void handleDelivery(Receiver receiver, DatagramPacket datagramPacket) {
        String data = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        String[] msg = data.split(Constants.DELIMITER);
        int id = Integer.parseInt(msg[0]);
        if (msg[1].equals(Constants.ACK)) {
            String firstMessage = msg[2] + Constants.DELIMITER + msg[3];
            if (!receiver.getDelivered().contains(firstMessage)) {
                Sender.addIdToReceive(firstMessage, id); // add again
            }
        }
        else {
            int myId = receiver.getClientId();
            if (!receiver.getDelivered().contains(data)) {
                Sender.addAllToReceive(data,id,myId);
            }
            String ack = myId + Constants.DELIMITER + Constants.ACK + Constants.DELIMITER + data;
            Pp2pEvents.pp2pBroadcast(receiver.getDatagramSocket(), myId, ack);
        }
    }

    public static void pp2pSendMessage(DatagramSocket sourceSocket, int destinationId, String msg) {
        try {
            SocketAddress socketAddress = Network.socketAddressHashMap.get(destinationId);
            sourceSocket.send(new DatagramPacket(msg.getBytes(), msg.length(), socketAddress));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void pp2pBroadcast(DatagramSocket sourceSocket, int sourceId, String msg) {
        try {
            for (Map.Entry<Integer, SocketAddress> destination : Network.socketAddressHashMap.entrySet()) {
                if (!destination.getKey().equals(sourceId)) {
                    DatagramPacket datagramPacket = new DatagramPacket(msg.getBytes(), msg.length(), destination.getValue());
                    sourceSocket.send(datagramPacket);
                }
            }
            if (!msg.split(Constants.DELIMITER)[1].equals(Constants.ACK)) {
                Utils.writeFiles(Network.getOutputPath(), "b " + msg.split(Constants.MSG)[1] + "\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


}