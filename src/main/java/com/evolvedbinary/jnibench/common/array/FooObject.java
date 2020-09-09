package com.evolvedbinary.jnibench.common.array;

public class FooObject {
  final String name;
  final long value;

  public FooObject(final String name, final long value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FooObject fooObject = (FooObject) o;

    if (value != fooObject.value) return false;
    return name.equals(fooObject.name);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (int) (value ^ (value >>> 32));
    return result;
  }
}
