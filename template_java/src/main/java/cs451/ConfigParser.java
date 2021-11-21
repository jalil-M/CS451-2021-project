package cs451;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigParser {

    private String path;

    private int nbMessages = 0;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        try{
            Scanner scanner = new Scanner(file);
            nbMessages = scanner.nextInt();
            System.out.println("Found the number of messages " + nbMessages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    public int getNbMessages() {
        return nbMessages;
    }

}
