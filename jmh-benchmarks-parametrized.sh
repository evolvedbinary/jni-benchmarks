#!/usr/bin/env bash


# Default argument values
POSITIONAL=()
ITERATIONS=1
BENCHMARK=ByteArrayFromNativeBenchmark
OUTPUT_DIRECTORY=
FILE_FORMAT=csv

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -i|--iterations)
    ITERATIONS="$2"
    shift
    shift
    ;;
    -b|--benchmark)
    BENCHMARK="$2"
    shift
    shift
    ;;
    -o|--output)
    OUTPUT_DIRECTORY="$2"
    shift
    shift
    ;;
    -f|--format)
    FILE_FORMAT="$2"
    shift
    shift
    ;;
    *)
    POSITIONAL+=("$1")
    shift
    ;;
esac
done
set -- "${POSITIONAL[@]}"

for i in `seq 1 $ITERATIONS`;
do
  echo -en "\rBenchmark run $i/$ITERATIONS"
  TIMESTAMP=$(date +"%s")
  java -Djava.library.path=target/jni-benchmarks-1.0.0-SNAPSHOT-application/jni-benchmarks-1.0.0-SNAPSHOT/lib -jar target/jni-benchmarks-1.0.0-SNAPSHOT-benchmarks.nar ${BENCHMARK} -rff ${OUTPUT_DIRECTORY}/${BENCHMARK}_${TIMESTAMP}.${FILE_FORMAT}
done;