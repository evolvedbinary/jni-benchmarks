#!/bin/bash
echo "FooByCall,FooByCallStatic,FooByCallInvoke,FooByCallFinal,FooByCallStaticFinal,FooByCallInvokeFinal" > call-benchmark.csv
for i in `seq 1 100`;
do
  echo -en "\rCallBenchmark run $i/100"
  target/jni-construction-benchmark-1.0-SNAPSHOT-application/jni-construction-benchmark-1.0-SNAPSHOT/bin/benchmark --benchmark=CallBenchmark --csv --iterations=1000000 >> call-benchmark.csv
done