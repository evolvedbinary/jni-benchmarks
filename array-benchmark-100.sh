#!/bin/bash
echo "AllocateInJavaGetArray,AllocateInJavaGetMutableArray,AllocateInJavaGet2DArray,AllocateInCppGetArray,AllocateInCppGet2DArray,AllocateInCppGet2DArrayListWrapper,AllocateInJavaGetArrayList,AllocateInCppGetArrayList" > array-benchmark.csv
for i in `seq 1 100`;
do
  echo -en "\rArrayBenchmark run $i/100"
  target/jni-construction-benchmark-1.0-SNAPSHOT-application/jni-construction-benchmark-1.0-SNAPSHOT/bin/benchmark --benchmark=ArrayBenchmark --csv --iterations=1000000 >> array-benchmark.csv
done
