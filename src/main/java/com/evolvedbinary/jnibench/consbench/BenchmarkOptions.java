package com.evolvedbinary.jnibench.consbench;

public class BenchmarkOptions {
  private final int iterations;
  private final boolean outputAsCSV;
  private final boolean inNs;
  private final boolean close;

  public BenchmarkOptions(int iterations, boolean outputAsCSV, boolean inNs, boolean close) {
    this.iterations = iterations;
    this.outputAsCSV = outputAsCSV;
    this.inNs = inNs;
    this.close = close;
  }

  public int getIterations() {
    return iterations;
  }

  public boolean isOutputAsCSV() {
    return outputAsCSV;
  }

  public boolean isInNs() {
    return inNs;
  }

  public boolean isClose() {
    return close;
  }
}
