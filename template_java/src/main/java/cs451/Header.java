package cs451;

public class Header {

    private final int source;
    private final int messageId;
    private final int destination;
    private final Type messageType;

    public Header(int messageId, Type messageType, int source, int destination) {
        this.source = source;
        this.messageId = messageId;
        this.messageType = messageType;
        this.destination = destination;
    }

    public Header(String text) {
        String[] splits = text.split(Constants.SEPARATOR_HEADER);
        this.messageId = Integer.parseInt(splits[0]);
        this.messageType = Type.valueOf(splits[1]);
        this.source = Integer.parseInt(splits[2]);
        this.destination = Integer.parseInt(splits[3]);
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getMessageId() {
        return messageId;
    }

    public Type getMessageType() {
        return messageType;
    }

}
