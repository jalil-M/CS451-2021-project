package cs451;

public class Message {

    public Header header;
    public String payload;

    public Message(String text) {
        int separator = text.indexOf("|");
        String headerText = text.substring(0, separator);
        this.header = new Header(headerText);
        this.payload = text.substring(separator+1);
    }

    public Message(int messageId, Type type, int source, int destination, String payload) {
        this.header = new Header(messageId, type, source, destination);
        this.payload = payload;
    }

    public Message(int id, Type messageType, int source, int destination) {
        this.header = new Header(id, messageType, source, destination);
    }

    public String serialize() {
        return String.format("%s|%s", header.serialize(), payload);
    }

    public boolean isMSG() {
        return header.isMSG();
    }

    public String broadcast() {
        return String.format("b %d", header.getMessageId());
    }

    public String deliver() {
        return String.format("d %d %d", header.getSource(), header.getMessageId());
    }
}
