package cs451;

public class Constants {
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

    // message constants
    public static final String REGEX_SOURCE_DESTINATION = "%d:%d";
    public static final String SEPARATOR_MESSAGE = "&";
    public static final String REGEX_HEADER = "%d:%s:%d:%d";
    public static final String REGEX_MESSAGE = "%s&%s";
    public static final String FORMAT_BROADCAST = "b %d";
    public static final String FORMAT_DELIVERY = "d %d %d";
    public static final String SEPARATOR_HEADER = ":";
    public static final String SEPARATOR_CONFIG = " ";

}
