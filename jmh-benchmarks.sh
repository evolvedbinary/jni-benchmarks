#!/usr/bin/env bash

java -Djava.library.path=target/jni-construction-benchmark-1.0-SNAPSHOT-application/jni-construction-benchmark-1.0-SNAPSHOT/lib -jar target/jni-construction-benchmark-1.0-SNAPSHOT-benchmarks.nar $@ -rf csv
