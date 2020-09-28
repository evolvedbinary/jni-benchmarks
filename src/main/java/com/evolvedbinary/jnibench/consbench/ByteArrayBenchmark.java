package com.evolvedbinary.jnibench.consbench;

import com.evolvedbinary.jnibench.common.bytearray.GetByteArray;

import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.outputResults;
import static com.evolvedbinary.jnibench.consbench.BenchmarkHelper.time;

public class ByteArrayBenchmark implements BenchmarkInterface {

  private static final String[] TEST_KEYS = {
      "testKeyWithReturnValueSize00000" + 10 + "Bytes",
      "testKeyWithReturnValueSize00000" + 50 + "Bytes",
      "testKeyWithReturnValueSize0000" + 512 + "Bytes",
      "testKeyWithReturnValueSize000" + 1024 + "Bytes",
      "testKeyWithReturnValueSize000" + 4 * 1024 + "Bytes",
      "testKeyWithReturnValueSize00" + 16 * 1024 + "Bytes"
  };

  @Override
  public void test(BenchmarkOptions benchmarkOptions) {
    int iterations = benchmarkOptions.getIterations();

    for (final String key : TEST_KEYS) {

      // User allocates and provides `key` in Java.
      // We allocate and fill `byte[] value` in C++ and return it.
      final long start1 = time(benchmarkOptions.isInNs());
      for (int j = 0; j < iterations; j++) {
        GetByteArray.get(key.getBytes(), 0, key.length());
      }
      final long end1 = time(benchmarkOptions.isInNs());

      String names[] = {
          "Basic get byte array, " + key
      };
      long results[] = {
          end1 - start1
      };

      outputResults(benchmarkOptions.isOutputAsCSV(), benchmarkOptions.isInNs(), names, results);
    }

  }
}
