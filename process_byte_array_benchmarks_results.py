import argparse

import numpy as np
import matplotlib.pyplot as plt
import os
import pandas as pd

def plot_performance_comparisons(results_dict):
	for k, benchmarks in results_dict.items():
		fig = plt.figure(num=None, figsize=(12, 8), dpi=80, facecolor='w', edgecolor='k')
		ax1 = fig.add_subplot(111)
		for name, samples in benchmarks.items():
			x = np.linspace(0, len(samples), len(samples))
			ax1.plot(x, samples, '-o', label = name)
			ax1.legend()
		plt.title("Performance comparison of getting byte array with " + str(k) +" bytes via JNI")
		plt.xlabel("Sample")
		plt.ylabel("Time [ns]")
		plt.savefig("fig" + str(k) +".png")
		# Why the below does not work?
		#ax1.figure.show()

# Columns:
# Benchmark	Mode	Threads	Samples	Score	Score Error (99.9%)	Unit	Param: valueSize
def process_value_results(path):
	# Example results_dict: { 10: { "benchmark1": [223, 243, 221, 219], "benchmark2": [566, ...], ... }, 50: { ... }, ... }
	results_dict = { }
	for file in os.listdir(path):
		if file.endswith(".csv"):
			fp = os.path.join(path, file)
			print(fp)
			df = pd.read_csv(fp)
			df = df.iloc[::9, :]
			df["Benchmark"] = df["Benchmark"].apply(lambda x: x.split('.')[-1])
			#df = df[df["Param: valueSize"] == 10]
			unique_sizes = df["Param: valueSize"].unique().tolist()
			for sz in unique_sizes:
				if sz not in results_dict:
					results_dict[sz] = {}
				df_for_sz = df[df["Param: valueSize"] == sz]
				# df_for_sz should be relatively small, so I allow myself to...
				# iterate through pandas dataframe *distant sound of crying baby*
				for index, row in df_for_sz.iterrows():
					if row["Benchmark"] not in results_dict[sz]:
						results_dict[sz][row["Benchmark"]] = []
					results_dict[sz][row["Benchmark"]].append(row["Score"])
			
	plot_performance_comparisons(results_dict)


def main():
	parser = argparse.ArgumentParser(description='Process JMH benchmarks result files (only SampleTime mode supported).')
	parser.add_argument('-p', '--path', type=str, help='Path to the directory')
	args = parser.parse_args()
	process_value_results(args.path)

if __name__ == "__main__":
	main()

