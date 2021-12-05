package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An implementation of external merge sort
 *
 * The sorting is composed of two phase:
 *
 * Partition: partition the input file  into a  bunch of smaller sorted file.
 * Merge: merge the sorted files into single file.
 */
public class ExternalMergeSort {

  /**
   * file size
   */
  static long totalSize;

  static Map<Integer, Long> passCountMap = new HashMap<>();

  public static void main(String[] args) throws IOException {
    if (args == null || args.length == 0) {
      System.out.println("input file name");
      System.exit(1);
    }
    String fileName = args[0];
    File file = new File(fileName);
    if (!file.exists()) {
      System.out.println("file name " + fileName + " not exist");
      System.exit(1);
    }
    if (file.length() == 0) {
      System.out.println("file is blank");
      System.exit(1);
    }
    totalSize = file.length();
    System.out.println("Total File Size : " + totalSize);
    // Pass 0
    int partitionCount = Partitioner.partition(file, 0);
    System.out.println("Split num : " + partitionCount);
    System.out.println("Pass 0 has complete.");
    // Pass 1+
    int pass = merge(partitionCount);

    // Compare Result
    StringBuilder data = new StringBuilder();
    try (BufferedReader r = new BufferedReader(new FileReader(file))) {
      String line = null;
      while ((line = r.readLine()) != null) {
        data.append(line);
      }
    }
    StringBuilder sortData = new StringBuilder();
    try (BufferedReader r = new BufferedReader(new FileReader("sorted-" + pass + "-0.txt"))) {
      String line = null;
      while ((line = r.readLine()) != null) {
        sortData.append(line);
      }
    }
    sortData.deleteCharAt(sortData.length() - 1);
    String originData = Stream.of(data.toString().split(",")).filter(x -> !x.isEmpty()).map(Integer::parseInt).sorted()
        .map(String::valueOf).collect(Collectors.joining(","));
    System.out.println("OriginData Length : " + originData.length());
    System.out.println("MergeSortData Length : " + sortData.length());
    System.out.println(originData.equals(sortData.toString()) ? "Compare OK" : "Oh my god!!!");
    for (int i = 0; i < Math.min(originData.length(), sortData.length()); i++) {
      if (originData.charAt(i) != sortData.charAt(i)) {
        System.out.println(originData.substring(i));
        System.out.println(sortData.substring(i));
        break;
      }
    }
  }


  /**
   * merge
   */
  static int merge(long n) throws IOException {
    int pass = 1;
    passCountMap.put(pass - 1, n);
    while (true) {
      sortedMerge(pass);
      if (passCountMap.get(pass) == 1) {
        break;
      }
      System.out.println("Pass " + pass + " has completed.");
      pass++;
    }
    System.out.println("Pass " + pass + "  is the last pass! Complete!");
    return pass;
  }

  public static void sortedMerge(int pass) throws IOException {
    long previousPassCount = passCountMap.get(pass - 1);
    long count = 0;
    for (int i = 1; i < previousPassCount; i += 2) {
      String f1 = "sorted-" + (pass - 1) + "-" + (i - 1) + ".txt";
      String f2 = "sorted-" + (pass - 1) + "-" + i + ".txt";
      System.out.println(f1 + " : " + f2);
      BufferedReader r1 = new BufferedReader(new FileReader(new File(f1)));
      BufferedReader r2 = new BufferedReader(new FileReader(new File(f2)));
      StringBuilder output = new StringBuilder();
      int size = Common.CHUNK_SIZE / 2;
      String content1 = readHalfOfChunkSize(r1, size);
      String content2 = readHalfOfChunkSize(r2, size);
      List<Integer> list1 = stringToIntList(content1);
      List<Integer> list2 = stringToIntList(content2);
      int l = 0, r = 0;
      while (content1.length() > 0 && content2.length() > 0) {
        while (l < list1.size() && r < list2.size()) {
          if (list1.get(l) < list2.get(r)) {
            output.append(list1.get(l++)).append(",");
          } else {
            output.append(list2.get(r++)).append(",");
          }
          if (output.length() > Common.CHUNK_SIZE) {
            Common.newSortFile("sorted-" + pass + "-" + count + ".txt", output.toString(), true);
            output = new StringBuilder();
          }
        }
        if (l == list1.size()) {
          l = 0;
          content1 = readHalfOfChunkSize(r1, size);
          list1 = stringToIntList(content1);
        }
        if (r == list2.size()) {
          r = 0;
          content2 = readHalfOfChunkSize(r2, size);
          list2 = stringToIntList(content2);
        }
      }
      if (l < list1.size()) {
        for (int p = l; p < list1.size(); p++) {
          output.append(list1.get(p)).append(",");
        }
        content1 = readHalfOfChunkSize(r1, size);
        while (content1.length() > 0) {
          list1 = stringToIntList(content1);
          for (Integer num : list1) {
            output.append(num).append(",");
          }
          content1 = readHalfOfChunkSize(r1, size);
        }
      }
      if (r < list2.size()) {
        for (int p = r; p < list2.size(); p++) {
          output.append(list2.get(p)).append(",");
        }
        content2 = readHalfOfChunkSize(r2, size);
        while (content2.length() > 0) {
          list2 = stringToIntList(content2);
          for (Integer num : list2) {
            output.append(num).append(",");
          }
          content2 = readHalfOfChunkSize(r2, size);
        }
      }
      if (output.length() > 0) {
        Common.newSortFile("sorted-" + pass + "-" + count + ".txt", output.toString(), true);
      }
      count++;
    }
    if (previousPassCount % 2 != 0) {
      try (BufferedReader r = new BufferedReader(
          new FileReader(new File("sorted-" + (pass - 1) + "-" + (previousPassCount - 1) + ".txt")))) {
        StringBuilder output = new StringBuilder();
        String line = null;
        while ((line = r.readLine()) != null) {
          output.append(line);
        }
        if (output.length() > 0) {
          Common.newSortFile("sorted-" + pass + "-" + count + ".txt", output.toString(), false);
          count++;
        }
      }
    }
    passCountMap.put(pass, count);
  }

  private static String readHalfOfChunkSize(BufferedReader reader, int length) throws IOException {
    List<Character> buf = new ArrayList<>();
    int ch = -1;
    StringBuilder sortedBuf = new StringBuilder();
    while ((ch = reader.read()) != -1) {
      buf.add((char) ch);
      if (buf.size() >= length && ch == ',') {
        sortedBuf.append(Common.charListToString(buf));
        break;
      }
    }
    if (ch == -1) {
      sortedBuf.append(Common.charListToString(buf));
    }
    return sortedBuf.toString();
  }

  private static List<Integer> stringToIntList(String content) {
    return Stream.of(content.split(",")).filter(s -> !s.isEmpty()).map(Integer::valueOf)
        .collect(Collectors.toList());
  }
}
