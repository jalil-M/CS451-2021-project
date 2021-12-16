package cs451.Network;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable, Comparable<Message> {

    public long time;
    public int msgId;
    public boolean ack;
    public byte[] data;
    public int basisId;
    public double count;
    public int sourceId;
    public int destinationId;

    public Message(int basisId, int msgId, int sourceId, int destinationId, boolean ack, long time, byte[] data) {
        this.ack = ack;
        this.count = 1;
        this.data = data;
        this.time = time;
        this.msgId = msgId;
        this.basisId = basisId;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
    }

    @Override
    public int compareTo(Message message) {
        int tmp1 = Integer.compare(sourceId, message.sourceId);
        int tmp2 = Integer.compare(destinationId, message.destinationId);
        int tmp3 = Integer.compare(basisId, message.basisId);
        int tmp4 = Integer.compare(msgId, message.msgId);
        return tmp1 != 0 ? tmp1 : tmp2 != 0 ? tmp2 : tmp3 != 0 ? tmp3 : tmp4;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Message tmp = (Message) obj;
        return msgId == tmp.msgId && basisId == tmp.basisId && sourceId == tmp.sourceId && destinationId == tmp.destinationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, destinationId, basisId, msgId);
    }

}
