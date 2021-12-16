package cs451.Network;

import cs451.Host;

import java.util.Arrays;
import java.util.List;

public class URB implements PerfectLinksDelivery {

    private final int id;
    private final int nbHosts;
    private final int[][] delivered;
    private final URBDelivery urbDelivery;
    private final PerfectLinks perfectLinks;

    public URB(int id, int nbMessage, List<Host> hosts, URBDelivery urbDelivery) throws Exception {
        this.id = id;
        this.nbHosts = hosts.size();
        this.urbDelivery = urbDelivery;
        this.perfectLinks = new PerfectLinks(id, nbMessage, hosts, this);
        delivered = new int[nbHosts][nbMessage];
        for (int[] array : delivered) {
            Arrays.fill(array, 0);
        }
    }

    public void stop() {
        perfectLinks.stop();
    }

    public void broadcast(int msgId, byte[] data) {
        for (int hostId = 0; hostId < nbHosts; hostId++) {
            if (hostId + 1 != id) {
                perfectLinks.send(id, msgId, id, hostId + 1, data);
            }
        }
    }

    @Override
    public void deliver(int basisId, int msgId, int sourceId, byte[] data) {
        delivered[basisId-1][msgId-1]++;
        if (delivered[basisId - 1][msgId - 1] == 1 && basisId != id) {
            for (int hostId = 1; hostId <= nbHosts; ++hostId) {
                if (hostId != id && hostId != basisId && hostId != sourceId) {
                    perfectLinks.send(basisId, msgId, id, hostId, data);
                }
            }
        }
        if (deliveryCheck(delivered[basisId-1][msgId-1])) {
            urbDelivery.deliver(basisId, msgId, data);
        }
    }

    private boolean deliveryCheck(int x) {
        boolean tmp1 = x + 1 > nbHosts / 2;
        boolean tmp2 = x <= nbHosts / 2;
        return tmp1 && tmp2;
    }

}