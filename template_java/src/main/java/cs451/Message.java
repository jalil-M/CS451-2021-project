package cs451;

public class Message {

    public Header header;
    public String payload;

    public Message(int messageId, Type messageType, int source, int destination) {
        this.header = new Header(messageId, messageType, source, destination);
    }

    public Message(int messageId, Type messageType, int source, int destination, String payload) {
        this.header = new Header(messageId, messageType, source, destination);
        this.payload = payload;
    }

    public Message(String text) {
        int separator = text.indexOf(Constants.SEPARATOR_MESSAGE);
        this.header = new Header(text.substring(0, separator));
        this.payload = text.substring(separator+1);
    }

    public String formatMessage() {
        String formattedHeader = String.format(Constants.REGEX_HEADER,
                header.getMessageId(),
                header.getMessageType(),
                header.getSource(),
                header.getDestination());
        return String.format(Constants.REGEX_MESSAGE,
                formattedHeader,
                payload);
    }

    public String broadcast() {
        return String.format(Constants.FORMAT_BROADCAST, header.getMessageId());
    }

    public String deliver() {
        return String.format(Constants.FORMAT_DELIVERY, header.getSource(), header.getMessageId());
    }
}
