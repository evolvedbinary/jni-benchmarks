# JNI Benchmarks

[![Build Status](https://dl.circleci.com/status-badge/img/gh/evolvedbinary/jni-benchmarks/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/evolvedbinary/jni-benchmarks/tree/main)
[![License](https://img.shields.io/badge/license-BSD%203-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

We provide the code for a small set of benchmarks to compare various approaches
to solving common JNI use-cases and then present the results.

The benchmarks at present are:

1. [com.evolvedbinary.jnibench.common.call](tree/main/src/main/java/com/evolvedbinary/jnibench/common/call) -
  Benchmarks for [Creating Objects with JNI](ObjectCreationBenchmarks.md)
  [(results)](ObjectCreationBenchmarks.md#object-creation-results).

2. [com.evolvedbinary.jnibench.common.array](tree/main/src/main/java/com/evolvedbinary/jnibench/common/array) -
  Benchmarks for [Passing Arrays with JNI](ArrayPassingBenchmarks.md)
  [(results)](ArrayPassingBenchmarks.md#array-passing-results).

3. [com.evolvedbinary.jnibench.common.bytearray](tree/main/src/main/java/com/evolvedbinary/jnibench/common/bytearray), and [com.evolvedbinary.jnibench.common.getputjni](tree/main/src/main/java/com/evolvedbinary/jnibench/common/getputjni) - Benchmarks for [JNI Data Transfer](DataBenchmarks.md)

# Reproducing

If you want to run the code yourself, you need to have Java 8, Maven 3, and a
C++ compiler that supports the C++ 11 standard. You can then simply run:

```bash
$ mvn clean compile package
```

In the `target/` sub-directory, you will then find both a
`jni-benchmarks-1.0.0-SNAPSHOT-application` folder and a
`jni-benchmarks-1.0.0-SNAPSHOT-application.zip` file, you can use either of
these. They both contain bash scripts in their `bin/` sub-folders for Mac,
Linux, Unix and batch scripts for Windows. These scripts will run a single
iteration of the benchmark.

If you want to run multiple iterations and get a CSV file of the results, you
can use `benchmark-100.sh` and/or `benchmark-100-with-close.sh`, or
`array-benchmark-100.sh`.

## JMH support

We have support for running the tests via JMH, see `jmh-benchmarks.sh`. You can
also pass `--help` to the script to see JMH options.

### Byte array benchmarks

There are two benchmarks, which are currently available only via JMH:
ByteArrayFromNativeBenchmark and ByteArrayToNativeBenchmark. They can be run
multiple times using `jmh-benchmarks-parametrized.sh` with:

```bash
./jmh-benchmarks-parametrized.sh -i 10 -b ByteArrayToNativeBenchmark -o results/ -f csv
```

Above command will run JMH with ByteArrayToNativeBenchmark benchmarks 10 times
and store result in CSV files in 'results' directory. You can also pass `--help`
to the script to see additional JMH options that can be passed.

<p>
Results can then be plotted using `process_byte_array_benchmarks_results.py` script.
For results of ByteArrayToNativeBenchmark benchmarks:
```bash
python3 process_byte_array_benchmarks_results.py -p results/ --param-name "Param: keySize" --chart-title "Performance comparison of passing byte array with {} bytes via JNI"
```
Command line parameter `p` expects path to directory with JMH result CSV files from running benchmarks with `jmh-benchmarks-parametrized.sh`.
The `{}` in `chart-title` parameter will be replaced by value from `param-name` column.
