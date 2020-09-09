package com.evolvedbinary.jnibench.consbench;

public class BenchmarkHelper {

  public static void outputResults(final boolean outputAsCSV, final boolean inNs, final String[] names, final long[] results) {
    assert (names.length == results.length);

    if (outputAsCSV) {
      StringBuilder stringBuilder = new StringBuilder();
      for (Long res: results) {
        stringBuilder.append(res);
        stringBuilder.append(',');
      }
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
      System.out.println(stringBuilder.toString());
    } else {
      final String timeUnits = timeUnits(inNs);
      for (int i = 0; i < names.length; ++i) {
        System.out.println(names[i] + ": " + results[i] + timeUnits);
      }
    }
  }

  public static long time(final boolean inNs) {
    if (inNs) {
      return System.nanoTime();
    } else {
      return System.currentTimeMillis();
    }
  }

  private static String timeUnits(final boolean inNs) {
    if (inNs) {
      return "ns";
    } else {
      return "ms";
    }
  }
}
