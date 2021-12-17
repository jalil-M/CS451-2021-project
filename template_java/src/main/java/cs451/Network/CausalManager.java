package cs451.Network;

import cs451.Constants;
import cs451.Host;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CausalManager implements URBDelivery, Broadcast {

    private Thread r;
    private final int id;
    private final int[] nbCausal;
    private final int[] vectorClock;
    private volatile boolean stopped;
    private final int[][] causalities;
    private final URB uniformReliableBroadcast;
    private final BroadcastDelivery broadcastDelivery;
    private final List<AwaitingMessage> awaitingMessages;
    private final BlockingQueue<AwaitingMessage> messageBlockingQueue;

    public CausalManager(int id, int nbMessages, List<Host> hosts, BroadcastDelivery broadcastDelivery, int[][] causalities) throws Exception {

        this.id = id;
        stopped = false;
        this.causalities = causalities;
        awaitingMessages = new LinkedList<>();
        this.broadcastDelivery = broadcastDelivery;
        messageBlockingQueue = new LinkedBlockingQueue<>();
        nbCausal = new int[hosts.size()];
        for (int hostId = 0; hostId < hosts.size(); hostId++) {
            nbCausal[hostId] = causalities[hostId].length;
        }
        vectorClock = new int[hosts.size()];
        Arrays.fill(vectorClock, 1);
        uniformReliableBroadcast = new URB(id, nbMessages, hosts, this);
        run();

    }

    private boolean deliveryCheck(AwaitingMessage awaitingMessage) {
        if (awaitingMessage.msgId != vectorClock[awaitingMessage.basisId-1]) {
            return false;
        }
        int tmp = 0;
        for (int clockId : causalities[awaitingMessage.basisId-1]) {
            if (awaitingMessage.vectorClock[tmp] > vectorClock[clockId-1]) {
                return false;
            }
            tmp++;
        }
        return true;
    }

    @Override
    public void deliver(int basisId, int msgId, byte[] data) {
        if (basisId == id) {
            return;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, 0, data.length);
        var content = Arrays.copyOfRange(byteBuffer.array(), nbCausal[basisId - 1] * 4, data.length);
        int[] tmpVectorClock = new int[nbCausal[basisId - 1]];
        for (int clockId = 0; clockId < nbCausal[basisId - 1]; ++clockId) {
            tmpVectorClock[clockId] = byteBuffer.getInt();
        }
        messageBlockingQueue.add(new AwaitingMessage(basisId, msgId, tmpVectorClock, content));
    }

    @Override
    public void broadcast(byte[] data) {
        int msgId = vectorClock[id - 1];
        broadcastDelivery.deliver(id, data);
        byte[] bytes = new byte[nbCausal[id - 1] * 4 + data.length]; // increase bytes
        ByteBuffer wrap = ByteBuffer.wrap(bytes, 0, bytes.length);
        for (int causalId : causalities[id - 1]) {
            wrap.putInt(vectorClock[causalId - 1]);
        }
        wrap.put(data);
        uniformReliableBroadcast.broadcast(msgId, wrap.array());
        vectorClock[id - 1]++;
    }

    @Override
    public void stop() {
        stopped = true;
        messageBlockingQueue.add(new AwaitingMessage(0, 0, null, null));
        uniformReliableBroadcast.stop();
        try {
            r.join();
        } catch (InterruptedException e) {
            System.out.println(Constants.ERROR_RECEIVING);
        }
    }

    private void handlePendingMessages() {
        boolean removed;
        do {
            removed = false;
            Iterator<AwaitingMessage> pendingMessageIterator = awaitingMessages.iterator();
            while (pendingMessageIterator.hasNext()) {
                AwaitingMessage m = pendingMessageIterator.next();
                if (deliveryCheck(m)) {
                    vectorClock[m.basisId - 1]++;
                    broadcastDelivery.deliver(m.basisId, m.data);
                    pendingMessageIterator.remove();
                    removed = true;
                }
            }
        } while (removed);
    }

    private void run() {
        r = new Thread(() -> {
            while (!stopped) {
                try {
                    var msg = messageBlockingQueue.take();
                    if (stopped) {
                        return;
                    }
                    if (deliveryCheck(msg)) {
                        vectorClock[msg.basisId - 1]++;
                        broadcastDelivery.deliver(msg.basisId, msg.data);
                        handlePendingMessages();
                    } else {
                        awaitingMessages.add(msg);
                    }
                } catch (InterruptedException e) {
                    System.out.println(Constants.ERROR_RECEIVING);
                    return;
                }
            }
        });
        r.start();
    }

    private static final class AwaitingMessage {

        public int msgId;
        public int basisId;
        public byte[] data;
        public int[] vectorClock;

        public AwaitingMessage(int basisId, int msgId, int[] vectorClock, byte[] data) {
            this.data = data;
            this.msgId = msgId;
            this.basisId = basisId;
            this.vectorClock = vectorClock;
        }
    }

}
