package cs451.Network;

public interface BroadcastDelivery {
    void deliver(int basisId, byte[] data);
}
