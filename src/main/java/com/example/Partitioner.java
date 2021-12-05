package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Slice a large file into  smaller files
 */
public class Partitioner {


  public static int partition(File file, int pass) throws IOException {
    int partitionCount = 0;
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      List<Character> chunk = new ArrayList<>();
      StringBuilder sortedBuffer = new StringBuilder();
      int ch = -1;
      while ((ch = bufferedReader.read()) != -1) {
        chunk.add((char) ch);
        if (chunk.size() > Common.CHUNK_SIZE && ch == ',') {
          String chunkStr = Common.charListToString(chunk);
          sortedBuffer.append(sortChunk(chunkStr));
          chunk.clear();
          Common.newSortFile("sorted-" + pass + "-" + (partitionCount++) + ".txt", sortedBuffer.toString(), false);
          sortedBuffer = new StringBuilder();
        }
      }
      if (chunk.size() > 0) {
        String chunkStr = Common.charListToString(chunk);
        sortedBuffer.append(sortChunk(chunkStr));
        chunk.clear();
        Common.newSortFile("sorted-" + pass + "-" + (partitionCount++) + ".txt", sortedBuffer.toString(), false);
      }
    }

    return partitionCount;
  }

  private static String sortChunk(String chunk) {
    String[] strArr = chunk.split(",");
    List<Integer> numbers = new ArrayList<>();
    for (String str : strArr) {
      try {
        int number = Integer.parseInt(str);
        numbers.add(number);
      } catch (Exception e) {
        System.out.println(str + "  is not number");
      }
    }
    return numbers.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
  }

}
