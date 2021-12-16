package cs451;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility Class");
    }

    public static final int ARG_LIMIT_CONFIG = 7;

    // indexes for id
    public static final int ID_KEY = 0;
    public static final int ID_VALUE = 1;

    // indexes for hosts
    public static final int HOSTS_KEY = 2;
    public static final int HOSTS_VALUE = 3;

    // indexes for output
    public static final int OUTPUT_KEY = 4;
    public static final int OUTPUT_VALUE = 5;

    // indexes for config
    public static final int CONFIG_VALUE = 6;

    // output files characters
    public static final String SPACE = " ";
    public static final String DELIVER = "d ";
    public static final String BROADCAST = "b ";

    // network limits
    public static final int DATA_SIZE = 5;
    public static final int MAX_SIZE = 64090;
    public static final int UDP_HEADER_SIZE = 8;
    public static final int BUFFER_SIZE = 65007 + 1;
    public static final long BREAK_SENDING = 50; // in microseconds
    public static final long TIMEOUT_SENDING = 300; // in microseconds
    public static final int MAX_SOCKET_SIZE = 100000000;

    // Error Messages
    public static final String ERROR_CONFIG = "Bad Config !";
    public static final String ERROR_SOCKET = "Socket Error !";
    public static final String ERROR_SENDING = "Sending stopped !";
    public static final String ERROR_RECEIVING = "Receiving stopped !";
    public static final String ERROR_HOST_INFO = "Cannot find host information !";

}
