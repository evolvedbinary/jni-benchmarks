#!/bin/bash
echo "FooByCall,FooByCallStatic,FooByCallInvoke,FooByCallFinal,FooByCallStaticFinal,FooByCallInvokeFinal" > call-benchmark.csv
for i in `seq 1 100`;
do
  echo -en "\rCallBenchmark run $i/100"
  target/jni-benchmarks-1.0.1-SNAPSHOT-application/jni-benchmarks-1.0.1-SNAPSHOT/bin/benchmark.sh --benchmark=CallBenchmark --csv --iterations=1000000 >> call-benchmark.csv
done
