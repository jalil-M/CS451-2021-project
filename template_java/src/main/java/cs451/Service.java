package cs451;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Service {

    private static String[] generate(int start, int end) {
        int diff = end-start;
        String[] strings = new String[diff];
        for (int i = 0; i < diff; i++) {
            strings[i] = Integer.toString(i+start);
        }
        return strings;
    }

    private static PerfectLink handleLink(PerfectLink perfectLink, int trackerMessage, int maxMessage) {
        if (perfectLink.getProcessId() == maxMessage) {
            return perfectLink;
        }
        for (String message: generate(0, trackerMessage)) {
            perfectLink.addMessage(message, maxMessage);
        }
        return perfectLink;
    }

    private static HashMap<Integer, InetSocketAddress> getAddresses(Parser parser) {
        HashMap<Integer, InetSocketAddress> addresses = new HashMap<>();
        for (Host host: parser.hosts()) {
            InetSocketAddress address = new InetSocketAddress(
                    host.getIp(), host.getPort());
            addresses.put(host.getId(), address);
        }
        return addresses;
    }

    public static PerfectLink createLink(Parser parser) throws IOException {
        int processId = parser.myId();
        HashMap<Integer, InetSocketAddress> addresses = getAddresses(parser);
        PerfectLink perfectLink = new PerfectLink(processId, addresses, parser.output());
        String[] splits = Files.readString(Path.of(parser.config())).split(Constants.SEPARATOR_CONFIG);
        return handleLink(perfectLink, Integer.parseInt(splits[0].strip()), Integer.parseInt(splits[1].strip()));
    }

}
