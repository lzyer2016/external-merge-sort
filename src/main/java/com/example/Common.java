package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Common {

  /**
   * 4KB
   */
  public static final int CHUNK_SIZE = 4 * 1024;

  public static String charListToString(List<Character> chunk) {
    StringBuilder stringBuilder = new StringBuilder();
    for (Character c : chunk) {
      stringBuilder.append(c);
    }
    return stringBuilder.toString();
  }

  public static void newSortFile(String sortedFileName, String content, boolean append) throws IOException {
    File file = new File(sortedFileName);
    if (file.exists() && !append) {
      System.out.println("Please clear " + sortedFileName);
      System.exit(-1);
    }
    try (FileWriter fileWriter = new FileWriter(file, append)) {
      fileWriter.write(content);
      fileWriter.flush();
    }
  }
}
