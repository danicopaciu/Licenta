package com.springapp.mvc.model.csv;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Daniel on 3/31/2015.
 */
public class Log {

    private static final String fileName = "log.txt";

    private static PrintWriter writer;

    public static void printLine(List<String> line) {
        if (writer == null) {
            try {
                writer = new PrintWriter(fileName, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.size(); i++) {
            sb.append(line.get(i));
            if (i != line.size() - 1) {
                sb.append(",");
            }
        }
        String input = sb.toString();
        writer.println(input);
    }

    public static void printLine(String s) {
        if (writer == null) {
            try {
                writer = new PrintWriter(fileName, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        writer.println(s);
    }

    public static void close() {
        writer.close();
    }
}
