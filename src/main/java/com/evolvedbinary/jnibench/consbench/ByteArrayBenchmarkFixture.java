package com.evolvedbinary.jnibench.consbench;

public class ByteArrayBenchmarkFixture implements BenchmarkFixture {

  private final String description;
  private final long duration;

  public ByteArrayBenchmarkFixture(final String description, final long duration) {
    this.description = description;
    this.duration = duration;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public long duration() {
    return duration;
  }
}
