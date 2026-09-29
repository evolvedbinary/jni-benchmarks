#!/usr/bin/env bash


# Default argument values
POSITIONAL=()
ITERATIONS=1
BENCHMARK=ByteArrayFromNativeBenchmark
OUTPUT_DIRECTORY=.
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

# --enable-preview is needed to run Java21 FFM
version=1.0.1
for i in `seq 1 $ITERATIONS`;
do
  echo -en "\rBenchmark run $i/$ITERATIONS"
  TIMESTAMP=$(date +"%s")
  java --enable-preview --enable-native-access=ALL-UNNAMED -Djava.library.path=target/jni-benchmarks-${version}-SNAPSHOT-application/jni-benchmarks-${version}-SNAPSHOT/lib -jar target/jni-benchmarks-${version}-SNAPSHOT-benchmarks.nar ${BENCHMARK} -rff ${OUTPUT_DIRECTORY}/${BENCHMARK}_${TIMESTAMP}.${FILE_FORMAT} ${POSITIONAL}
done;
