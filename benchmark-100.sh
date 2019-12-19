#!/bin/bash
echo "FooByCall,FooByCallStatic,FooByCallInvoke,FooByCallFinal,FooByCallStaticFinal,FooByCallInvokeFinal" > benchmark.csv
for i in `seq 1 100`;
do
  target/jni-construction-benchmark-1.0-SNAPSHOT-application/jni-construction-benchmark-1.0-SNAPSHOT/bin/benchmark --csv --iterations=1000000 >> benchmark.csv
done    
