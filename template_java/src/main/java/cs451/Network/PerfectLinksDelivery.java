package cs451.Network;

public interface PerfectLinksDelivery {
    void deliver(int basisId, int msgId, int sourceId, byte[] data);
}