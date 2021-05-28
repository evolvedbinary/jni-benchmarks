#!/usr/bin/env python3
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
from datetime import datetime
from collections import namedtuple
from json.decoder import JSONDecodeError
import pathlib
from typing import Dict, List, NewType, Sequence, Tuple

import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
from pandas.core.frame import DataFrame
import re
from sys import maxsize
import json

# Types
Params = NewType('Params', dict[str, str])
BMParams = namedtuple("BMParams", "primary secondary")
Benchmark = NewType('Benchmark', str)
BMResult = namedtuple("BMResult", "value, score, error")
ResultSet = NewType('ResultSet', dict[Benchmark, Sequence[BMResult]])
ResultSets = NewType('ResultSets', dict[Tuple, ResultSet])

const_datetime_str = datetime.today().isoformat()


class RunnerError(Exception):
    """Base class for exceptions in this module."""

    def __init__(self, message: str):
        self.message = message


def error(message: str):
    raise RunnerError(message)


def uncomment(line: str) -> bool:
    if line.strip().startswith('#'):
        return False
    return True


def read_config_file(configFile: pathlib.Path):
    lines = [line.strip()
             for line in configFile.open().readlines() if uncomment(line)]
    try:
        return json.loads('\n'.join(lines))
    except JSONDecodeError as e:
        error(
            f'JSON config file {configFile} ({configFile.absolute()}) error {str(e)}')


def optional(key: str, dict: Dict, op=None):
    if key in dict:
        if op:
            return op(dict[key])
        else:
            return dict[key]
    else:
        return None


def required(key: str, dict: Dict):
    if key in dict:
        return dict[key]
    else:
        error(f'{key} missing from JMH config')


# read files, merge allegedly similar results
#
# return a single data frame
#


def normalize_data_frame_from_path(path: pathlib.Path):
    files = []
    if path.is_dir():
        files = sorted(path.rglob("*.csv"))
    else:
        files.append(path)

    normalized = None
    for file in files:
        try:
            df = pd.read_csv(file)
        except pd.errors.EmptyDataError:
            break

        # every 9th line is the interesting one, discard the rest
        df = df.iloc[::9, :]
        df["Benchmark"] = df["Benchmark"].apply(lambda x: x.split('.')[-1])
        if normalized is None:
            normalized = df
        else:
            normalized = pd.merge(normalized, df)
    if normalized is None:
        raise RunnerError(f'No csv file(s) found at {path}')
    if normalized.empty:
        error(f'No CSV files, or all empty, in {path}')
    return normalized

# Decide which columns are params (they are labelled "Param: <name>")
# return a map of { <name> : <label> } e.g. { "valueSize": "Param: valueSize", "checksum": "Param: checksum"}


def extract_params(dataframe: DataFrame) -> Params:
    params = {}
    for column in dataframe.columns:
        fields = column.split(':')
        if len(fields) == 2 and fields[0] == 'Param':
            params[fields[1].strip()] = column

    return params

# Separate the param map into a single entry dictionary (primary)
# and a multi-entry (all the rest)


def split_params(params: Params, primary_param_name: str) -> BMParams:

    if not primary_param_name in params.keys():
        raise RunnerError(
            f'Missing {primary_param_name} in params {params}')
    primary_params = {primary_param_name: params[primary_param_name]}

    del params[primary_param_name]
    secondary_params = params

    return BMParams(primary=primary_params, secondary=secondary_params)


# Dictionary indexed by the tuple of secondary parameter values
# For the fixed tuple, indexed by each benchmark
# within each benchmark, a namedtuple of { value:, score:, error:, } for the value of the primary parameter, its score and its error

def extract_results_per_param(dataframe: DataFrame, params: BMParams) -> ResultSets:
    resultSets: ResultSets = {}
    for _, row in dataframe.iterrows():
        secondaryTuple = tuple_of_secondary_values(params, row)
        if not secondaryTuple in resultSets.keys():
            resultSets[secondaryTuple] = {}
        if not row['Benchmark'] in resultSets[secondaryTuple].keys():
            resultSets[secondaryTuple][row['Benchmark']] = []
        for _, column in params.primary.items():
            entry = BMResult(
                value=row[column], score=row['Score'], error=row['Score Error (99.9%)'])
            resultSets[secondaryTuple][row['Benchmark']].append(entry)

    return resultSets


def tuple_of_secondary_values(params: BMParams, row: np.row_stack) -> Tuple:
    secondaryValues = []
    for _, column in params.secondary.items():
        secondaryValues.append(row[column])
    return tuple(secondaryValues)


def tuple_of_secondary_keys(params: BMParams) -> Tuple:
    secondaryKeys = []
    for key, _ in params.secondary.items():
        secondaryKeys.append(key)
    return tuple(secondaryKeys)


def plot_all_results(params: BMParams, resultSets: ResultSets, path, include_benchmarks: str, exclude_benchmarks: str, label: str) -> None:
    indexKeys = tuple_of_secondary_keys(params)
    for indexTuple, resultSet in resultSets.items():
        plot_result_set(indexKeys, indexTuple, resultSet,
                        path, include_benchmarks, exclude_benchmarks, label)


def plot_result_axis_errorbars(ax, resultSet: ResultSet) -> None:
    ax.set_xscale('log')
    ax.set_yscale('log')

    uplimits = [True, False] * 5
    lolimits = [False, True] * 5

    for benchmark, results in resultSet.items():
        count = len(results)
        values = [result.value for result in results]
        offsets = list(range(count))
        scores = [result.score for result in results]
        errors = [result.error for result in results]

        swaplimits = uplimits
        uplimits = lolimits
        lolimits = swaplimits

        ax.errorbar(np.array(values), np.array(scores),
                    uplims=uplimits[:count], lolims=lolimits[:count],
                    yerr=np.array(errors), label=benchmark, capsize=6.0, capthick=1.5)


def plot_result_axis_bars(ax, resultSet: ResultSet) -> None:

    ax.set_xscale('log')

    barCount = 0
    for index, result in resultSet.items():
        barCount = len(result)
        break

    widths = [0 for _ in range(barCount+1)]
    bmIndex = -(barCount/2)

    for benchmark, results in resultSet.items():

        # The first iteration will have widths == [0,0,...] so will mimic bmIndex==0, so don't do it twice...
        if bmIndex == 0:
            bmIndex = bmIndex + 1

        xs = [result.value for result in results]
        pairs = list(zip(widths, results))
        x2s = [vw[1].value + vw[0]*0.67*bmIndex for vw in pairs]
        ys = [result.score for result in results]

        bottoms = [result.score - result.error/2 for result in results]
        heights = [result.error for result in results]
        widths = [result.value/(barCount*3) for result in results]
        ax.bar(x=x2s, bottom=bottoms, height=heights, alpha=0.8,
               width=widths, label=benchmark)

        ax.plot(xs, ys)
        bmIndex = bmIndex + 1


def plot_result_set(indexKeys: Tuple, indexTuple: Tuple, resultSet: ResultSet, path: pathlib.Path, include_benchmarks: str, exclude_benchmarks: str, label: str):
    fig = plt.figure(num=None, figsize=(18, 12), dpi=80,
                     facecolor='w', edgecolor='k')
    ax = plt.subplot()

    plot_result_axis_bars(ax, resultSet)

    plt.title(
        f'{str(indexKeys)}={str(indexTuple)} include={include_benchmarks} exclude={exclude_benchmarks}')
    plt.xlabel("X")
    plt.ylabel("t (ns)")
    plt.legend(loc='lower right')
    plt.grid(b='True', which='both')

    name = f'fig_{"_".join([str(t) for t in indexTuple])}_{label}.png'

    if path.is_file():
        path = path.parent()
    fig.savefig(path.joinpath(name))


alpha_pattern = re.compile(f'[A-Za-z0-9_\-+]')


def check_benchmark_alpha(benchmark: str):
    if alpha_pattern.match(benchmark) is None:
        raise RunnerError(
            f'The benchmark pattern {benchmark} has non-alphanumeric characters')


def filter_for_benchmarks(dataframe: DataFrame, include_benchmarks, exclude_benchmarks) -> DataFrame:

    filteredframe = dataframe

    if include_benchmarks is not None:
        for include in include_benchmarks:
            check_benchmark_alpha(include)
        include_translated = '|'.join(include_benchmarks)
        pattern = re.compile(f'\S*({include_translated})\S*')
        filteredframe = filteredframe[filteredframe['Benchmark'].apply(
            lambda x: pattern.match(x) is not None)]

    if exclude_benchmarks is not None:
        for exclude in exclude_benchmarks:
            check_benchmark_alpha(exclude)
        exclude_translated = '|'.join(exclude_benchmarks)
        pattern = re.compile(f'\S*({exclude_translated})\S*')
        filteredframe = filteredframe[filteredframe['Benchmark'].apply(
            lambda x: pattern.match(x) is None)]

    return filteredframe


def filter_for_range(dataframe: DataFrame, xaxisparam: Dict) -> DataFrame:

    param_name = required('name', xaxisparam)
    xmin = optional('min', xaxisparam, lambda x: int(x))
    xmax = optional('max', xaxisparam, lambda x: int(x))
    if xmax is None and xmin is None:
        return dataframe

    if xmax is None:
        xmax = maxsize
    if xmin is None:
        xmin = -maxsize

    if xmin > xmax:
        raise RunnerError(f'The range {xmin} to {xmax} is not valid')

    return dataframe[dataframe[f'Param: {param_name}'].apply(
        lambda x: int(x) >= xmin and int(x) <= xmax)]


def process_some_plots(path: pathlib.Path, plot: Dict) -> None:

    xaxisparam = required('xaxisparam', plot)
    primary_param_name = required('name', xaxisparam)

    include_benchmarks = optional('include_patterns', plot)
    exclude_benchmarks = optional('exclude_patterns', plot)
    label = required('label', plot)

    dataframe = normalize_data_frame_from_path(path)
    if len(dataframe) == 0:
        raise RunnerError(
            f'0 results were read from the file(s) at {path} ({path.absolute})')

    dataframe = filter_for_benchmarks(
        dataframe, include_benchmarks, exclude_benchmarks)
    if len(dataframe) == 0:
        raise RunnerError(
            f'0 results after filtering benchmarks include: {include_benchmarks}, exclude: {exclude_benchmarks}')

    dataframe = filter_for_range(dataframe, xaxisparam)
    if len(dataframe) == 0:
        raise RunnerError(
            f'0 results after filtering range {xaxisparam}')

    params: BMParams = split_params(
        extract_params(dataframe), primary_param_name)
    resultSets = extract_results_per_param(dataframe, params)
    plot_all_results(params, resultSets, path,
                     include_benchmarks, exclude_benchmarks, label)


def process_benchmarks(config: Dict) -> None:
    path = pathlib.Path(required('result.path', config))
    if not path.exists():
        raise RunnerError(f'The plot directory/file {path} does not exist')

    for plot in required('plots', config):
        process_some_plots(path, plot)

# Columns:
# Benchmark	Mode	Threads	Samples	Score	Score Error (99.9%)	Unit	Param: valueSize


# Example usage:
# ./jmhplot.py -c jmh_plot.json -f analysis/testplots


def main():

    parser = argparse.ArgumentParser(
        description='Process JMH benchmarks result files (only SampleTime mode supported).')
    parser.add_argument(
        '-c', '--config', help='A JSON configuration file for the JMH plot(s)', default='jmh_plot.json')
    parser.add_argument(
        '-f', '--file', help='A directory or CSV file with the output to process')
    args = parser.parse_args()
    try:
        config_file = pathlib.Path(args.config)
        if not config_file.exists():
            raise RunnerError(
                f'The config file {config_file} does not exist')
        if not config_file.is_file():
            raise RunnerError(
                f'The config file {config_file} is not a text file')
        config = read_config_file(config_file)

        # Override the config if a path is supplied
        if args.file:
            config['result.path'] = args.file

        plot_index = 1
        process_benchmarks(config)
    except RunnerError as error:
        print(
            f'JMH process benchmarks ({pathlib.Path(__file__).name}) error: {error.message}')


if __name__ == "__main__":
    main()
