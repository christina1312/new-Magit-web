package System;
import java.io.*;

public class Utility {
    public static String readContentsOfFile( String fileName) throws IOException {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName)))) {

            return in.readLine();
        }
    }

    public static void writeToFile(String message, String fileName) throws IOException {
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
            out.write(message);

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) { }
            }
        }
    }
}

