package cs451;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

	private Utils() {
		throw new IllegalStateException("Utility Class");
	}

	// Method to clean the output files
	public static void cleanFiles(String path) throws IOException {

		File file = new File(path);
		if (!file.exists()){
			if (file.createNewFile()){
				System.out.println("Creating file..." + file.getName());
			} else {
				System.err.println("file exists !");
			}
		}
		else {
			new FileWriter(file.getAbsoluteFile(),false).close();
		}

	}

	// Method to write in the output files
	public static void writeFiles(String path, String data) throws IOException {
		File file = new File(path);
		if (!file.exists()){
			file.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(data);
		bufferedWriter.close();
	}

}