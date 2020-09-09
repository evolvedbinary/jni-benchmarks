package com.evolvedbinary.jnibench.common.array;

import java.util.*;

public class Jni2DGetArrayListWrapper extends Jni2DGetArray {

  @Override
  public List<FooObject> getObjectList(final NativeObjectArray<FooObject> nativeObjectArray) {
    final Object[][] objArr = get2DArray(nativeObjectArray.get_nativeHandle());
    return new FooObjectList(objArr);
  }

  public static class FooObjectList implements List<FooObject> {

    private final Object[][] backingObjects;

    /**
     * Cache so that we only instantiate 1 FooObject
     * for each object representation in the backingObjects
     */
    private FooObject[] cachedObjects;

    private FooObjectList(final Object[][] backingObjects) {
      this.backingObjects = backingObjects;
    }

    @Override
    public int size() {
      return backingObjects.length;
    }

    @Override
    public boolean isEmpty() {
      return backingObjects.length == 0;
    }

    @Override
    public boolean contains(final Object o) {
      if (o == null || !(o instanceof FooObject)) {
        return false;
      }

      final FooObject other = (FooObject)o;

      // first, search any cachedObjects
      int lastCachedIdx = -1;
      if (cachedObjects != null) {
        for (int i = 0; i < cachedObjects.length; i++) {
          final FooObject cachedObject = cachedObjects[i];
          if (cachedObject == null) {
            break;
          }

          if (cachedObject.equals(other)) {
            return true;
          }

          lastCachedIdx = i;
        }
      }

      // second, search any uncached objects after the cached objects
      for (int i = lastCachedIdx + 1; i < backingObjects.length; i++) {
        return ((String)backingObjects[i][0]).equals(other.name)
                && ((long)backingObjects[i][1]) == other.value;
      }

      return false;
    }

    @Override
    public Iterator<FooObject> iterator() {
      return listIterator();
    }

    @Override
    public Object[] toArray() {
      final FooObject[] array = new FooObject[backingObjects.length];
      return toArray(array);
    }

    @Override
    public <T> T[] toArray(final T[] a) {
      final FooObject[] array;
      if (a.length < backingObjects.length) {
        array = new FooObject[backingObjects.length];
      } else {
        array = (FooObject[]) a;
      }

      // first, process any cachedObjects
      int lastCachedIdx = -1;
      if (cachedObjects != null) {
        for (int i = 0; i < cachedObjects.length; i++) {
          final FooObject cachedObject = cachedObjects[i];
          if (cachedObject == null) {
            break;
          }

          array[i] = cachedObject;

          lastCachedIdx = i;
        }
      }

      // second, process any uncached objects after the cached objects
      for (int i = lastCachedIdx + 1; i < backingObjects.length; i++) {
        array[i] = getOrCreateFooObject(i);
      }

      return (T[])array;
    }

    private FooObject getOrCreateFooObject(final int index) {
      if (cachedObjects == null) {
        cachedObjects = new FooObject[backingObjects.length];
      }

      FooObject object = cachedObjects[index];
      if (object != null) {
        return object;
      }

      object = new FooObject((String)backingObjects[index][0], (long)backingObjects[index][1]);

      cachedObjects[index] = object;

      return object;
    }

    @Override
    public boolean add(final FooObject fooObject) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public boolean remove(final Object o) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public boolean containsAll(final Collection<?> other) {
      for (final Object o : other) {
        if (!contains(o)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean addAll(final Collection<? extends FooObject> c) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends FooObject> c) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public FooObject get(final int index) {
      return getOrCreateFooObject(index);
    }

    @Override
    public FooObject set(final int index, final FooObject element) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public void add(final int index, final FooObject element) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public FooObject remove(final int index) {
      throw new UnsupportedOperationException("List is immutable");
    }

    @Override
    public int indexOf(final Object o) {
      if (o == null || !(o instanceof FooObject)) {
        return -1;
      }

      final FooObject other = (FooObject)o;

      // first, search any cachedObjects
      int lastCachedIdx = -1;
      if (cachedObjects != null) {
        for (int i = 0; i < cachedObjects.length; i++) {
          final FooObject cachedObject = cachedObjects[i];
          if (cachedObject == null) {
            break;
          }

          if (cachedObject.equals(other)) {
            return i;
          }

          lastCachedIdx = i;
        }
      }

      // second, search any uncached objects after the cached objects
      for (int i = lastCachedIdx + 1; i < backingObjects.length; i++) {
        if (((String)backingObjects[i][0]).equals(other.name)
                && ((long)backingObjects[i][1]) == other.value) {
          return i;
        }
      }

      return -1;
    }

    @Override
    public int lastIndexOf(final Object o) {
      if (o == null || !(o instanceof FooObject)) {
        return -1;
      }

      final FooObject other = (FooObject)o;

      // first, search any cachedObjects
      int cachedIdx = -1;
      if (cachedObjects != null) {
        for (int i = cachedObjects.length - 1; i >= 0; i--) {
          final FooObject cachedObject = cachedObjects[i];
          if (cachedObject == null) {
            continue;
          }

          if (cachedObject.equals(other)) {
            cachedIdx = i;
            break;
          }
        }
      }

      // second, search any uncached objects after the cached objects
      for (int i = backingObjects.length - 1; i >= 0; i--) {
        if (cachedIdx > i) {
          return cachedIdx;
        }

        if (((String)backingObjects[i][0]).equals(other.name)
                && ((long)backingObjects[i][1]) == other.value) {
          return i;
        }
      }

      return -1;
    }

    @Override
    public ListIterator<FooObject> listIterator() {
      return listIterator(0);
    }

    @Override
    public ListIterator<FooObject> listIterator(final int firstOffset) {
      return new ListIterator<FooObject>() {
        private int offset = firstOffset;

        @Override
        public boolean hasNext() {
          return offset < backingObjects.length;
        }

        @Override
        public FooObject next() {
          if (offset >= backingObjects.length) {
            throw new NoSuchElementException();
          }
          return getOrCreateFooObject(offset++);
        }

        @Override
        public boolean hasPrevious() {
          return offset > 0 && backingObjects.length > 0;
        }

        @Override
        public FooObject previous() {
          if (offset <= 0 || backingObjects.length == 0) {
            throw new NoSuchElementException();
          }
          return getOrCreateFooObject(offset--);
        }

        @Override
        public int nextIndex() {
          if (offset < backingObjects.length) {
            return offset + 1;
          }
          return backingObjects.length;
        }

        @Override
        public int previousIndex() {
          if (offset == 0 || backingObjects.length == 0) {
            return -1;
          }

          return offset - 1;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("List is immutable");
        }

        @Override
        public void set(final FooObject fooObject) {
          throw new UnsupportedOperationException("List is immutable");
        }

        @Override
        public void add(final FooObject fooObject) {
          throw new UnsupportedOperationException("List is immutable");
        }
      };
    }

    @Override
    public List<FooObject> subList(final int fromIndex, final int toIndex) {
      throw new UnsupportedOperationException();
    }
  }
}
