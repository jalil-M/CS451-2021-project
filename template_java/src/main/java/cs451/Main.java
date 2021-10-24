package cs451;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Main {
    private static PerfectLink perfectLink;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        perfectLink.stop();

        //write/flush output file if necessary
        System.out.println("Writing output.");
        perfectLink.saveFile();
        System.out.flush();
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static String[] generate(int start, int end) {
        String[] strings = new String[end-start];
        for (int i = 0; i < end-start; i++) {
            strings[i] = Integer.toString(i+start);
        }
        return strings;
    }

    private static PerfectLink createLink(Parser parser) throws IOException {
        int processId = parser.myId();
        HashMap<Integer, InetSocketAddress> addresses = new HashMap<>();
        for (Host host: parser.hosts()) {
            InetSocketAddress address = new InetSocketAddress(
                    host.getIp(), host.getPort());
            addresses.put(host.getId(), address);
        }
        PerfectLink perfectLink = new PerfectLink(processId, addresses, parser.output());
        int trackerMessage = 0;
        int maxMessage = 0;
        String config = Files.readString(Path.of(parser.config()));
        String[] splits = config.split(" ");
        trackerMessage = Integer.parseInt(splits[0].strip());
        maxMessage = Integer.parseInt(splits[1].strip());
        if (perfectLink.getProcessId() == maxMessage) {
            return perfectLink;
        }
        String[] messages = generate(0, trackerMessage);
        for (String message: messages) {
            perfectLink.send(message, maxMessage);
        }
        return perfectLink;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Creating the perfect links...");
        perfectLink = createLink(parser);

        System.out.println("Broadcasting and delivering messages...\n");
        perfectLink.start();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
