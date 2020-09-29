#!/usr/bin/env bash

java -Djava.library.path=target/jni-benchmarks-1.0.0-SNAPSHOT-application/jni-benchmarks-1.0.0-SNAPSHOT/lib -jar target/jni-benchmarks-1.0.0-SNAPSHOT-benchmarks.nar $@ -rf csv
