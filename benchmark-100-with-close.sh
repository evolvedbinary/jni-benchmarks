#!/bin/bash
version=1.0.1
echo "FooByCall,FooByCallStatic,FooByCallInvoke,FooByCallFinal,FooByCallStaticFinal,FooByCallInvokeFinal" > call-benchmark-with-close.csv
for i in `seq 1 100`;
do
  echo -en "\rCallBenchmark run $i/100"
  target/jni-benchmarks-${version}-SNAPSHOT-application/jni-benchmarks-${version}-SNAPSHOT/bin/benchmark.sh --csv --close --iterations=1000000 >> call-benchmark-with-close.csv
done
