# Building/running the tests

* Added an explicit `aol.properties` to build for `MacOS/aarch64` (i.e. Apple M1.. class)
* installed `sdkman` to make switching JDKs easy

To build using, and then run using, a particular Java version:
```bash
$ sdk use java 25.0.4+1.1-librca
$ mvn clean compile package
$ ./jmh-benchmarks-parametrized.sh -i 1 -b ByteArrayFromNativeBenchmarkJava25 -o results -f csv
```
Or to run everything:
