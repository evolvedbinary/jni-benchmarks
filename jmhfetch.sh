#!/bin/bash
user=apaxton@inoltre.evolvedbinary.com:/home/apaxton/jni-benchmarks/
resultdir=${1}
scp -r ${user}/results/${resultdir} results/${resultdir}
./jmhplot.py --config jmh_plot.json --file results/${resultdir}/${resultdir}.csv