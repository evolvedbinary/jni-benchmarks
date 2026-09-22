#!/usr/bin/env bash

version=1.0.1
java --enable-preview --enable-native-access=ALL-UNNAMED -Djava.library.path=target/jni-benchmarks-${version}-SNAPSHOT-application/jni-benchmarks-${version}-SNAPSHOT/lib -jar target/jni-benchmarks-${version}-SNAPSHOT-benchmarks.nar $@ -rf csv
