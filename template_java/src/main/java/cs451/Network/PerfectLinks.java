package cs451.Network;

import cs451.Constants;
import cs451.Host;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import static cs451.Constants.BREAK_SENDING;
import static cs451.Constants.TIMEOUT_SENDING;

public class PerfectLinks implements SocketDelivery {

    private Thread sender;
    private final int nbHost;
    private Thread[] threads;
    private final Socket socket;
    private final int nbReceiver;
    private final int maxSending;
    private volatile boolean stop = false;
    private final HashSet<Integer>[][] delivered;
    private final PerfectLinksDelivery perfectLinksDelivery;
    private final LinkedBlockingQueue<Message>[] linkedBlockingQueues;
    private final ConcurrentSkipListSet<Message>[] concurrentSkipListSets;

    public PerfectLinks(int id, int nbMessage, List<Host> hosts, PerfectLinksDelivery perfectLinksDelivery) throws Exception {

        this.perfectLinksDelivery = perfectLinksDelivery;

        nbHost = hosts.size();
        maxSending = Math.max(5000 / (nbHost * nbHost), 1);
        nbReceiver = Math.max(Math.min(Runtime.getRuntime().availableProcessors() - 3, hosts.size()), 1);

        concurrentSkipListSets = new ConcurrentSkipListSet[nbHost];
        hosts.forEach(h -> concurrentSkipListSets[h.getId() - 1] = new ConcurrentSkipListSet<>());

        linkedBlockingQueues = new LinkedBlockingQueue[nbReceiver];
        for (int receiverId = 0; receiverId < nbReceiver; ++receiverId) {
            linkedBlockingQueues[receiverId] = new LinkedBlockingQueue<>();
        }

        delivered = new HashSet[nbHost][nbMessage];
        for (int hostId = 0; hostId < nbHost; ++hostId) {
            for (int messageId = 0; messageId < nbMessage; ++messageId) {
                delivered[hostId][messageId] = new HashSet<>();
            }
        }

        Optional<Host> host = hosts.stream().filter(h -> h.getId() == id).findFirst();
        if (host.isEmpty()) {
            throw new Exception(Constants.ERROR_HOST_INFO);
        }

        this.socket = new Socket(host.get().getIp(), host.get().getPort(), this, hosts);
        initReceiving();
        initSending();

    }

    public void send(int basisId, int msgId, int sourceId, int destinationId, byte[] data) {
        Message message = new Message(basisId, msgId, sourceId, destinationId, false, System.currentTimeMillis() + TIMEOUT_SENDING, data);
        concurrentSkipListSets[destinationId-1].add(message);
        socket.send(message);
    }

    public void stop() {
        stop = true;
        socket.stop();
        for (int receiverId = 0; receiverId < nbReceiver; ++receiverId) {
            linkedBlockingQueues[receiverId].add(new Message(0, 0, 0, 0, false, 0, null));
        }
        for (int receiverId = 0; receiverId < nbReceiver; ++receiverId) {
            try {
                threads[receiverId].join();
            } catch (InterruptedException interruptedException) {
                System.out.println(Constants.ERROR_RECEIVING);
            }
        }
        try {
            sender.join();
        } catch (InterruptedException interruptedException) {
            System.out.println(Constants.ERROR_SENDING);
        }
    }

    private void initReceiving() {
        threads = new Thread[nbReceiver];
        for (int receiverId = 0; receiverId < nbReceiver; ++receiverId) {
            final int threadId = receiverId;
            threads[receiverId] = new Thread(() -> {
                while (!stop) {
                    try {
                        Message message = linkedBlockingQueues[threadId].take();
                        if (stop) {
                            return;
                        }
                        int sourceId = message.ack ? message.destinationId : message.sourceId;
                        if (message.ack) {
                            for (Message msg : concurrentSkipListSets[sourceId-1]) {
                                if (msg.equals(message)) {
                                    message.data = msg.data;
                                    break;
                                }
                            }
                            concurrentSkipListSets[message.destinationId-1].remove(message);
                        } else {
                            message.ack = true;
                            socket.send(message);
                        }
                        if (!delivered[message.basisId-1][message.msgId-1].add(sourceId)) {
                            continue;
                        }
                        perfectLinksDelivery.deliver(message.basisId, message.msgId, sourceId, message.data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[receiverId].start();
        }
    }

    private void initSending() {
        sender = new Thread(() -> {
            while (!stop) {
                try {
                    Thread.sleep(BREAK_SENDING);
                    if (stop) {
                        return;
                    }
                    for (int hostId = 0; hostId < this.nbHost; ++hostId) {
                        long currentTime = System.currentTimeMillis();
                        concurrentSkipListSets[hostId].stream().filter(msg -> currentTime > msg.time).limit(maxSending).forEach(msg -> {
                            socket.send(msg);
                            msg.count++;
                            long randomness = (long) (Math.random() * 100) + TIMEOUT_SENDING * (long) Math.pow(1.2, msg.count);
                            msg.time = currentTime + randomness;
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println(Constants.ERROR_SENDING);
                }
            }
        });
        sender.start();
    }

    @Override
    public void deliver(Message msg) {
        linkedBlockingQueues[msg.basisId % nbReceiver].add(msg);
    }

}
