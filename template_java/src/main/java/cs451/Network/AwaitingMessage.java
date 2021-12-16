package cs451.Network;

public final class AwaitingMessage {

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