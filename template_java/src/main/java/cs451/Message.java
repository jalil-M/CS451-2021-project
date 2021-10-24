package cs451;

public class Message {

    public Header header;
    public String payload;

    public Message(int messageId, Type messageType, int source, int destination) {
        this.header = new Header(messageId, messageType, source, destination);
    }

    public Message(int messageId, Type type, int source, int destination, String payload) {
        this.header = new Header(messageId, type, source, destination);
        this.payload = payload;
    }

    public Message(String text) {
        int separator = text.indexOf(Constants.SEPARATOR_MESSAGE);
        this.header = new Header(text.substring(0, separator));
        this.payload = text.substring(separator+1);
    }

    public boolean isMSG() {
        return Type.MSG == header.getMessageType();
    }

    private String formatHeader(int messageId, int source, int destination, Type messageType) {
        return String.format(Constants.REGEX_HEADER,
                messageId,
                messageType,
                source,
                destination);
    }

    public String formatMessage() {
        return String.format(Constants.REGEX_MESSAGE,
                formatHeader(header.getMessageId(), header.getSource(), header.getDestination(), header.getMessageType()),
                payload);
    }

    public String broadcast() {
        return String.format(Constants.FORMAT_BROADCAST, header.getMessageId());
    }

    public String deliver() {
        return String.format(Constants.FORMAT_DELIVERY, header.getSource(), header.getMessageId());
    }
}
