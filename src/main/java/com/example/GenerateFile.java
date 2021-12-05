package com.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * generate file
 */
public class GenerateFile {

  /**
   * 40KB
   */
  static final Integer FILE_SIZE = 40 * 1024;

  public static void main(String[] args) throws IOException {
    String data = "data.txt";
    File file = new File(data);
    if (file.exists()) {
      file.delete();
    }
    if (file.createNewFile()) {
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
      StringBuilder content;
      while (file.length() < FILE_SIZE) {
        content = new StringBuilder();
        for (int i = 0; i < 25; i++) {
          content.append(ThreadLocalRandom.current().nextInt(1000)).append(",");
        }
        bufferedWriter.write(content.toString());
        bufferedWriter.flush();
      }
      bufferedWriter.close();
      System.out.println("data.txt size = " + file.length() + " generate success");
    }
  }
}
