package cs451.Network;

public interface URBDelivery {
    void deliver(int basisId, int msgId, byte[] data);
}
