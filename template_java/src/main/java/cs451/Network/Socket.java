package cs451.Network;

import cs451.Constants;
import cs451.Host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static cs451.Constants.BUFFER_SIZE;
import static cs451.Constants.UDP_HEADER_SIZE;

public class Socket {

    private Thread s;
    private Thread r;
    private ByteBuffer byteBuffer;
    private volatile boolean ending;
    private DatagramSocket datagramSocket;
    private final SocketDelivery socketDelivery;
    private final DatagramPacket datagramPacket;
    private final DatagramPacket[] datagramPackets;
    private final BlockingQueue<Message> messageBlockingQueue;
    private final ByteBuffer allocatedByteBuffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.BIG_ENDIAN);

    public Socket(String ipAddress, int port, SocketDelivery socketDelivery, List<Host> hosts) {

        this.socketDelivery = socketDelivery;
        try {
            this.datagramSocket = new DatagramSocket(port);
            datagramSocket.setReceiveBufferSize(Constants.MAX_SOCKET_SIZE);
        } catch (SocketException e) {
            System.out.println(Constants.ERROR_SOCKET);
        }

        messageBlockingQueue = new LinkedBlockingQueue<>();
        datagramPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE, new InetSocketAddress(ipAddress, port));
        byteBuffer = ByteBuffer.wrap(datagramPacket.getData(), 0, BUFFER_SIZE);

        datagramPackets = new DatagramPacket[hosts.size()];
        hosts.forEach(h -> datagramPackets[h.getId() - 1] = new DatagramPacket(allocatedByteBuffer.array(), 0, new InetSocketAddress(h.getIp(), h.getPort())));
        initReceiver();
        initSender();

    }

    public void stop() {
        ending = true;
        datagramSocket.close();
        try {
            r.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            messageBlockingQueue.add(new Message(0, 0, 0, 0, false, 0, null));
            s.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void send(Message message) {
        messageBlockingQueue.add(message);
    }

    private void checkEnding() {
        if (ending) {
            return;
        }
    }

    private void initSender() {
        s = new Thread(() -> {
            while (!ending) {
                try {
                    Message msg = messageBlockingQueue.take();
                    checkEnding();
                    allocatedByteBuffer.clear();
                    allocatedByteBuffer.put((byte) msg.basisId);
                    allocatedByteBuffer.putInt(msg.msgId);
                    allocatedByteBuffer.put((byte) msg.sourceId);
                    allocatedByteBuffer.put((byte) msg.destinationId);
                    allocatedByteBuffer.put((byte) (msg.ack ? 1 : 0));
                    if (!msg.ack) {
                        allocatedByteBuffer.put(msg.data);
                    }
                    int destinationId = msg.ack ? msg.sourceId : msg.destinationId;
                    DatagramPacket p = datagramPackets[destinationId-1];
                    p.setData(allocatedByteBuffer.array(), 0, allocatedByteBuffer.position());
                    datagramSocket.send(p);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        s.start();
    }

    private void initReceiver() {
        r = new Thread(() -> {
            while (!ending) {
                try {
                    byte[] data = null;
                    datagramSocket.receive(datagramPacket);
                    byteBuffer = ByteBuffer.wrap(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
                    byteBuffer.clear();
                    int basisId = byteBuffer.get();
                    int msgId = byteBuffer.getInt();
                    int sourceId = byteBuffer.get();
                    int destinationId = byteBuffer.get();
                    boolean ack = byteBuffer.get() == (byte) 1;
                    if (!ack) {
                        data = Arrays.copyOfRange(byteBuffer.array(), UDP_HEADER_SIZE, datagramPacket.getLength());
                    }
                    socketDelivery.deliver(new Message(basisId, msgId, sourceId, destinationId, ack, 0, data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        r.start();
    }

}
