# Thoughts on the New RocksDB Java (FFM) API

## Resources

- [Adam's Presentation](https://evolvedbinary.slides.com/adamretter/rocksjava-present-and-future)
- [JNI Spec](https://docs.oracle.com/en/java/javase/17/docs/specs/jni/functions.html)
- [Prototype Blog Post](https://rocksdb.org/blog/2024/02/20/foreign-function-interface.html)
- [Prototype PR](https://github.com/facebook/rocksdb/pull/11095)
- [FFM API](https://docs.oracle.com/en/java/javase/25/core/foreign-function-and-memory-api.html)
- [FFM JEP 454](https://openjdk.org/jeps/454)

## Ground Rules

- We don't want a big bang change. That is to say, we will keep supporting Java 8, and the associated JNI-based
API, for the foreseeable future. In parallel, we will introduce an FFM-based API. This API will initially support
only core functions, and will be considered experimental; it will be grown incrementally so that at a point,
we will be able to deprecate and remove the JNI API.

