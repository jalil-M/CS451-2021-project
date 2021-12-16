package cs451.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileUtils implements Writer {

    private final String outputPath;
    private static FileUtils fileUtils;
    private final ConcurrentLinkedQueue<String> queue;

    private FileUtils(String outputPath) {
        this.outputPath = outputPath;
        this.queue = new ConcurrentLinkedQueue<>();
        Runtime.getRuntime().addShutdownHook(new Thread(this::dump));
    }

    public static synchronized FileUtils getInstance(String outputPath) {
        if (fileUtils == null) {
            fileUtils = new FileUtils(outputPath);
        }
        return fileUtils;
    }

    public void dump() {
        try {
            FileWriter fileWriter = new FileWriter(outputPath, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            queue.forEach(elem -> {
                try {
                    bufferedWriter.write(elem);
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String log) {
        queue.add(log);
    }

}
