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
import pathlib
import json
import subprocess
from typing import Dict


class JMHRunnerError(Exception):
    """Base class for exceptions in this module."""

    def __init__(self, message: str):
        self.message = message


def error(message: str):
    raise JMHRunnerError(message)


def read_config_file(configFile: pathlib.Path):
    with open(configFile) as json_file:
        return json.load(json_file)


def optional(key: str, dict: Dict):
    if key in dict:
        return dict[key]
    else:
        return None


def required(key: str, dict: Dict):
    if key in dict:
        return dict[key]
    else:
        error(f'{key} missing from JMH config')


option_map = {'batchsize': 'bs',
              'iterations': 'i', 'forks': 'f', 'time': 'r', 'timeout': 'to',
              'timeunit': 'tu', 'verbosity': 'v',
              'warmupbatchsize': 'wbs', 'warmupforks': 'wf',
              'warmupiterations': 'wi', 'warmuptime': 'w',
              'warmupmode': 'wm'}

const_datetime_str = datetime.today().isoformat()


def output_log_file(config: Dict):
    path = pathlib.Path('.')
    path_str = optional('result.path', config)
    if path_str:
        path = pathlib.Path(path_str)
        if not path.exists():
            error(f'result.path: {path_str} does not exist')
        if not path.is_dir():
            error(f'result.path: {path_str} is not a directory')
    return path.joinpath(pathlib.Path(f'jmh_{const_datetime_str}.md'))


def output_options(config: Dict) -> list:
    path = pathlib.Path('.')
    path_str = optional('result.path', config)
    if path_str:
        path = pathlib.Path(path_str)
        if not path.exists():
            error(f'result.path: {path_str} does not exist')
        if not path.is_dir():
            error(f'result.path: {path_str} is not a directory')
    filepath = path.joinpath(pathlib.Path(f'jmh_{const_datetime_str}.csv'))
    return ['-rff', str(filepath)]


def build_jmh_command(config: Dict) -> list:

    cmd = ["java"]
    jvm_args = optional('jvmargs', config)
    if jvm_args:
        if not type(jvm_args) is list:
            error('jvmargs field must be a list of arguments')
        for arg_value in jvm_args:
            cmd.append(f'-{arg_value}')
    java_library_path = optional('java.library.path', config)
    if java_library_path:
        cmd.append(f'-Djava.library.path={java_library_path}')
    jar = optional('jar', config)
    if jar:
        cmd.append('-jar')
        cmd.append(jar)
    help = optional('help', config)
    if help:
        cmd.append('-h')
    benchmark = required('benchmark', config)
    cmd.append(str(benchmark))
    params = optional('params', config)
    if params:
        if not type(params) is dict:
            error('params field must be a dictionary of parameters')
        for key, value in params.items():
            if type(value) is int or type(value) is float:
                value = str(value)
            if type(value) is list:
                value_str = ','.join([str(v) for v in value])
                cmd.append('-p')
                cmd.append(f'{key}={value_str}')
            elif type(value) is str:
                cmd.append('-p')
                cmd.append(f'{key}={str(value)}')
            else:
                error(f'field {key} does not have a string or list value')

    options = optional('options', config)
    if options:
        if not type(options) is dict:
            error('Options field must be a dictionary of parameters')
        for key, value in options.items():
            if key not in option_map:
                error(f'Option {key} is not a valid/known option')
            if type(value) is int or type(value) is float:
                value = str(value)
            if type(value) is not str:
                error(
                    f'Options field {key} must have a string value, not: {value}')
            cmd.append(f'-{option_map[key]}')
            cmd.append(value)

    cmd.extend(output_options(config))

    return cmd


def log_jmh_session(cmd: list, config: Dict, config_file: str):
    output_file = pathlib.Path(output_log_file(config))
    if output_file.exists():
        error(f'Output file {output_file} already exists')

    with output_file.open(mode='w', encoding='UTF-8') as log:
        log.writelines(line + '\n' for line in
                       ['## JMH Run', f'This JMH run was generated on {const_datetime_str}'])
        log.writelines(line + '\n' for line in
                       [f'#### Config', f'The configuration was read from `{config_file}`', '```json'])
        json.dump(config, fp=log, indent=4)
        log.write('\n')
        log.writelines(line + '\n' for line in
                       ['```', '#### Command', 'The java command executed to run the tests', '```', ' '.join(cmd), '```'])


def exec_jmh_cmd(cmd: list, help_requested):
    cmd_str = ' '.join(cmd)
    if help_requested:
        print(f'JMH Help requested, command: {cmd_str}')
    else:
        print(f'Execute: {cmd_str}')
    subprocess.run(cmd)


def main():
    parser = argparse.ArgumentParser(description='Run configured jmh tests.')
    parser.add_argument(
        '-c', '--config', help='A JSON configuration file for the JMH run', default='jmh.json')

    args = parser.parse_args()
    try:
        config_file = pathlib.Path(args.config)
        if not config_file.exists():
            raise JMHRunnerError(
                f'The config file {config_file} does not exist')
        if not config_file.is_file():
            raise JMHRunnerError(
                f'The config file {config_file} is not a text file')
        config = read_config_file(config_file)
        cmd_list = build_jmh_command(config)

        log_jmh_session(cmd_list, config, f'{config_file.resolve()}')
        exec_jmh_cmd(cmd_list, optional('help', config))

    except JMHRunnerError as error:
        print(
            f'JMH pyrunner ({pathlib.Path(__file__).name}) error: {error.message}')


if __name__ == "__main__":
    main()
