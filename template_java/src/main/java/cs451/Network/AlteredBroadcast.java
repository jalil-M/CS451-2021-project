package cs451.Network;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static cs451.Constants.DATA_SIZE;
import static cs451.Constants.MAX_SIZE;

public class AlteredBroadcast implements BroadcastDelivery {

    byte[] data;
    ByteBuffer byteBuffer;
    private Broadcast broadcast;
    private final AlteredBroadcastDelivery alteredBroadcastDelivery;

    public AlteredBroadcast(AlteredBroadcastDelivery alteredBroadcastDelivery) {
        data = new byte[MAX_SIZE];
        this.alteredBroadcastDelivery = alteredBroadcastDelivery;
        byteBuffer = ByteBuffer.wrap(data, 0, data.length);
    }

    public void init(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    public void stop() {
        broadcast.stop();
    }

    @Override
    public void deliver(int basisId, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, data.length);
        for (int i = 0; i < (data.length / DATA_SIZE); ++i) {
            basisId = buffer.get();
            alteredBroadcastDelivery.deliver(basisId, buffer.getInt());
        }
    }

    public void broadcast(int basisId, int data) {
        byteBuffer.put((byte) basisId);
        byteBuffer.putInt(data);
        if (byteBuffer.position() == MAX_SIZE) {
            flush();
        }
    }

    public static int computeMessageSize(int nbMessage) {
        return (int) Math.ceil(( nbMessage / (float) MAX_SIZE) * DATA_SIZE);
    }

    public void flush() {
        if (byteBuffer.position() > 0) {
            broadcast.broadcast(Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.position()));
            byteBuffer.clear();
        }
    }

}
