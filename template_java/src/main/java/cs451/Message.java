package cs451;


public class Message{
    private int id;
    private int msgNumber;

    public Message(int id, int msgNumber){
        this.id = id;
        this.msgNumber = msgNumber;
    }

    public String toString(){
        return id + Constants.DELIMITER + Constants.MSG + msgNumber;
    }

}