#!/usr/bin/env bash

version=1.0.1
java -Djava.library.path=target/jni-benchmarks-${version}-SNAPSHOT-application/jni-benchmarks-${version}-SNAPSHOT/lib -jar target/jni-benchmarks-${version}-SNAPSHOT-benchmarks.nar $@ -rf csv
