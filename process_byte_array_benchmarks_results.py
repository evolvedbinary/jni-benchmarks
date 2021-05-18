#!/usr/local/bin/python3
#
# Copyright © 2016, Evolved Binary Ltd
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#       names of its contributors may be used to endorse or promote products
#       derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

import argparse

import numpy as np
import matplotlib.pyplot as plt
import os
import pandas as pd


class BenchmarkError(Exception):
    """Base class for exceptions in this module."""
    pass


# read files, merge allegedly similar results
#
# return a single data frame
#


def normalize_data_frame_from_path(path):
    normalized = None
    for file in os.listdir(path):
        if file.endswith(".csv"):
            fp = os.path.join(path, file)
            df = pd.read_csv(fp)
            # every 9th line is the interesting one, discard the rest
            df = df.iloc[::9, :]
            df["Benchmark"] = df["Benchmark"].apply(lambda x: x.split('.')[-1])
            if normalized is None:
                normalized = df
            else:
                normalized = pd.merge(normalized, df)
    return normalized

# Decide which columns are params (they are labelled "Param: <name>")
# return a map of { <name> : <label> } e.g. { "valueSize": "Param: valueSize", "checksum": "Param: checksum"}


def extract_params(dataframe):
    params = {}
    for column in dataframe.columns:
        fields = column.split(':')
        if len(fields) == 2 and fields[0] == 'Param':
            params[fields[1].strip()] = column

    return params

# Separate the param map into a single entry dictionary (primary)
# and a multi-entry (all the rest)


def split_params(params, primary_param_name):
    primary_param = {primary_param_name: params[primary_param_name]}
    del params[primary_param_name]
    secondary_params = params
    return {"primary": primary_param, "secondary": secondary_params}


# Dictionary indexed by the tuple of secondary parameter values
# For the fixed tuple, indexed by each benchmark
# within each benchmark, a list of { value:, score:, error:, } for the value of the primary parameter, its score and its error

def extract_results_per_param(dataframe, params):
    resultSets = {}
    for index, row in dataframe.iterrows():
        secondaryTuple = tuple_of_secondary_values(params, row)
        if not secondaryTuple in resultSets.keys():
            resultSets[secondaryTuple] = {}
        if not row['Benchmark'] in resultSets[secondaryTuple].keys():
            resultSets[secondaryTuple][row['Benchmark']] = []
        for key, column in params['primary'].items():
            entry = {'value': row[column], 'score': row['Score'],
                     'error': row['Score Error (99.9%)']}
            resultSets[secondaryTuple][row['Benchmark']].append(entry)

    return resultSets


def tuple_of_secondary_values(params, row):
    secondaryValues = []
    for key, column in params['secondary'].items():
        secondaryValues.append(row[column])
    return tuple(secondaryValues)


def tuple_of_secondary_keys(params):
    secondaryKeys = []
    for key, column in params['secondary'].items():
        secondaryKeys.append(key)
    return tuple(secondaryKeys)


def plot_all_results(params, resultSets, path):
    indexKeys = tuple_of_secondary_keys(params)
    for indexTuple, resultSet in resultSets.items():
        plot_result_set(indexKeys, indexTuple, resultSet, path)


def plot_result_set(indexKeys, indexTuple, resultSet, path):
    results = list(resultSet.values())[0]
    paramValues = [result['value'] for result in results]

    fig = plt.figure(num=None, figsize=(12, 8), dpi=80,
                     facecolor='w', edgecolor='k')
    ax = plt.subplot()
    ax.set_xscale('log')
    ax.set_yscale('log')

    for benchmark, results in resultSet.items():
        values = [result['value'] for result in results]
        scores = [result['score'] for result in results]
        errors = [result['error'] for result in results]
        ax.errorbar(np.array(values), np.array(scores),
                    yerr=np.array(errors), label=benchmark)

    plt.title(str(indexKeys) + "=" + str(indexTuple))
    plt.xlabel("X")
    plt.ylabel("t (ns)")
    plt.legend(loc='lower right')
    name = "fig"
    for k in list(indexKeys):
        name = name + "_" + str(k)
    for k in list(indexTuple):
        name = name + "_" + str(k)
    fig.savefig(path + name)


def process_benchmarks(path, primary_param_name):
    dataframe = normalize_data_frame_from_path(path)
    params = split_params(extract_params(dataframe), primary_param_name)
    resultSets = extract_results_per_param(dataframe, params)
    plot_all_results(params, resultSets, path)


# Columns:
# Benchmark	Mode	Threads	Samples	Score	Score Error (99.9%)	Unit	Param: valueSize


# Example usage:
# python process_byte_array_benchmarks_results.py -p results_dir/ --param-name "Param: valueSize" --chart-title "Performance comparison of getting byte array with {} bytes via JNI"
def main():
    parser = argparse.ArgumentParser(
        description='Process JMH benchmarks result files (only SampleTime mode supported).')
    parser.add_argument('-p', '--path', type=str,
                        help='Path to the directory with benchmarking results generated by JMH run', default='/Users/alan/swProjects/evolvedBinary/jni-benchmarks/plotthis/')
    parser.add_argument('--param-name', type=str,
                        help='Benchmarks parameter name', default='valueSize')
    parser.add_argument('--chart-title', type=str, help='Charts\' title',
                        default='Performance comparison of getting byte array with {} bytes via JNI')
    args = parser.parse_args()

    # process_value_results(args.path, args.param_name, args.chart_title)
    process_benchmarks(args.path, args.param_name)


if __name__ == "__main__":
    main()
