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

    public static PerfectLink createLink(Parser parser) throws IOException {
        int maxMessage = 0;
        int trackerMessage = 0;
        int processId = parser.myId();
        HashMap<Integer, InetSocketAddress> addresses = new HashMap<>();
        for (Host host: parser.hosts()) {
            InetSocketAddress address = new InetSocketAddress(
                    host.getIp(), host.getPort());
            addresses.put(host.getId(), address);
        }
        PerfectLink perfectLink = new PerfectLink(processId, addresses, parser.output());
        String[] splits = Files.readString(Path.of(parser.config())).split(Constants.SEPARATOR_CONFIG);
        trackerMessage = Integer.parseInt(splits[0].strip());
        maxMessage = Integer.parseInt(splits[1].strip());
        if (perfectLink.getProcessId() == maxMessage) {
            return perfectLink;
        }
        for (String message: generate(0, trackerMessage)) {
            perfectLink.send(message, maxMessage);
        }
        return perfectLink;
    }

}
