#include <jni.h>

#include <algorithm>
#include <cassert>
#include <cstring>
#include <iostream>
#include <unordered_map>
#include <vector>

#include "com_evolvedbinary_jnibench_common_bytearray_GetByteArray.h"

static const std::string STR_10_B = "pkDHTxmMR1";
static const std::string STR_50_B = "pkDHTxmMR18N2l9k88EmLgN7cCCTt9rWksb1fEBw397vi5Ug1Y";
static const std::string STR_512_B = "pkDHTxmMR18N2l9k88EmLgN7cCCTt9rWksb1fEBw397vi5Ug1YHC3UAVUAoB9VYjCxYhzZSrWPc5IuZAHeCAyATJA7KAQghSS6yHTEyqRPja76aCbaANbTUbOdZf97vP1hVIlHw3UVRSQrSrFT4gmP61qTUnOD3FlOMKV8DoUS6i7OPDfHjIUd7AxPoBShF3tRCCPFqhYkFVxSWSa4YsXTtIiVI10NsjcujpyONKwQdhh005uMnFgUOCpW3fhkC9UkoGyzQiEsGli4eQGHVkchnF3elYElXZLAd7xug1cka8e4OkAhJaFwf6QETVqoszoLva3PPTzqRTid1g9A6Cua6BePUI4C8gLt6D8MWv0mEWD33C4xEmN9nsO50I5wpHdjKZteKjGM4IGxK8iNkwMMcaHQhDDKIgrQ6buEAzR47XpFTOS38cDa1LqYMrgUMNkGoSKnHbfEwLKFXa7T3AtuJXGVFmnPxNVcgfDl5iqrryvEOqXFoEzyc3HffvuT6F";
static const std::string STR_1_KB = "pkDHTxmMR18N2l9k88EmLgN7cCCTt9rWksb1fEBw397vi5Ug1YHC3UAVUAoB9VYjCxYhzZSrWPc5IuZAHeCAyATJA7KAQghSS6yHTEyqRPja76aCbaANbTUbOdZf97vP1hVIlHw3UVRSQrSrFT4gmP61qTUnOD3FlOMKV8DoUS6i7OPDfHjIUd7AxPoBShF3tRCCPFqhYkFVxSWSa4YsXTtIiVI10NsjcujpyONKwQdhh005uMnFgUOCpW3fhkC9UkoGyzQiEsGli4eQGHVkchnF3elYElXZLAd7xug1cka8e4OkAhJaFwf6QETVqoszoLva3PPTzqRTid1g9A6Cua6BePUI4C8gLt6D8MWv0mEWD33C4xEmN9nsO50I5wpHdjKZteKjGM4IGxK8iNkwMMcaHQhDDKIgrQ6buEAzR47XpFTOS38cDa1LqYMrgUMNkGoSKnHbfEwLKFXa7T3AtuJXGVFmnPxNVcgfDl5iqrryvEOqXFoEzyc3HffvuT6FtciwEdUsJCg2EsiayMcl82fGX8zFPvI6MpsOIB4PBYHDGnb2y4cww70I3pLHZUDj9wxFvrVwFaxJCPC0Jek3ZbBQENXbfbAcLvh6a0qdPbnRqnFxFppcEqsJ1GiUfjwqSSktIOMVpxmUarFdVu5ZZmiOqFjLoTz4lXnsj0DMlogCTmdoUZEtBk8pph0R0nMaAxIhlJtNxOPHystIFv2EW0t9VhPJKZjIMppyzZBknOpb4ZhAKZCeNwcgRrqcGNkS1OEolFOOT4nMRKMZiM4v6WSOB6qFHOX8acmBfzPxt09ABJZjVRSSligkcKpkWDgxdHy86Ctn2sl118YNOrdzP9asHDQdGuQh1FevHNY767zv6N8K4b7IYVyfZC9dwN9oSdXXouVirIUlVStOsrUEAIkXKh16uyiBR6W3qpcVxw7HCqVsVpxUvVr32gzkUWjjQ6d4l5PXrKerzzaKcNcLiKEYo369Ngida9X94lWjv0RknptD2LOYTSu8Ko5XKdzi";
static const std::string STR_4_KB = "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";
static const std::string STR_16_KB = "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";

static const auto DB_MOCK = std::unordered_map<std::string, std::string>{
  {"testKeyWithReturnValueSize0000010Bytes", STR_10_B},
  {"testKeyWithReturnValueSize0000050Bytes", STR_50_B},
  {"testKeyWithReturnValueSize0000512Bytes", STR_512_B},
  {"testKeyWithReturnValueSize0001024Bytes", STR_1_KB},
  {"testKeyWithReturnValueSize0004096Bytes", STR_4_KB},
  {"testKeyWithReturnValueSize0016384Bytes", STR_16_KB}
};

static const std::string& GetByteArrayInternal(const char* key) {
  std::string str(key, 38);
  //std::cerr << std::endl << "Getting " << str << std::endl << std::endl;
  return DB_MOCK.at(str);
}

static jbyteArray StringToJavaByteArray(JNIEnv *env, const std::string& str) {
  const jsize jlen = static_cast<jsize>(str.size());
  jbyteArray jbytes = env->NewByteArray(jlen);
  if(jbytes == nullptr) {
    // exception thrown: OutOfMemoryError
    return nullptr;
  }

  env->SetByteArrayRegion(jbytes, 0, jlen,
    const_cast<jbyte*>(reinterpret_cast<const jbyte*>(str.c_str())));
  if(env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    env->DeleteLocalRef(jbytes);
    return nullptr;
  }

  return jbytes;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    get
 * Signature: ([BII)[B
 */
jbyteArray Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_get___3BII
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len) {
  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    delete[] key;
    return nullptr;
  }

  // Mock getting value
  std::string value = GetByteArrayInternal(reinterpret_cast<char*>(key));

  // Cleanup
  delete[] key;

  jbyteArray jret_value = StringToJavaByteArray(env, value);
  if (jret_value == nullptr) {
    // exception occurred
    return nullptr;
  }

  return jret_value;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    get
 * Signature: ([BII[BII)I
 */
jint Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_get___3BII_3BII
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval, jint jval_off, jint jval_len) {
  static const int kStatusError = -2;

  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: OutOfMemoryError
    delete[] key;
    return kStatusError;
  }

  std::string cvalue = GetByteArrayInternal(reinterpret_cast<char*>(key));

  // cleanup
  delete[] key;

  const jint cvalue_len = static_cast<jint>(cvalue.size());
  const jint length = std::min(jval_len, cvalue_len);

  env->SetByteArrayRegion(
      jval, jval_off, length,
      const_cast<jbyte*>(reinterpret_cast<const jbyte*>(cvalue.c_str())));
  if (env->ExceptionCheck()) {
    // exception thrown: OutOfMemoryError
    return kStatusError;
  }

  return cvalue_len;
}

static inline void SetByteBufferData(JNIEnv* env, const jmethodID jarray_mid, const jobject& jbuf,
    const char* content, const size_t content_len) {
  jbyteArray jarray = static_cast<jbyteArray>(env->CallObjectMethod(jbuf, jarray_mid));
  if (env->ExceptionCheck()) {
    // exception occurred
    env->DeleteLocalRef(jbuf);
    return;
  }

  jboolean is_copy = JNI_FALSE;
  jbyte* ja = reinterpret_cast<jbyte*>(
      env->GetPrimitiveArrayCritical(jarray, &is_copy));
  if (ja == nullptr) {
    // exception occurred
     env->DeleteLocalRef(jarray);
     env->DeleteLocalRef(jbuf);
     return;
  }

  memcpy(ja, const_cast<char*>(content), content_len);

  env->ReleasePrimitiveArrayCritical(jarray, ja, 0);

  env->DeleteLocalRef(jarray);
}

static inline jint SetDirectByteBufferData(char* buf, const jint jbuf_len,
    const char* data, const size_t data_size) {
  const jint jdata_len = static_cast<jint>(data_size);
  const jint length = std::min(jbuf_len, jdata_len);

  memcpy(buf, data, length);

  return jdata_len;
}

static inline jobject NewByteBuffer(JNIEnv* env, const size_t capacity, const char* content) {
  const jclass jclazz = env->FindClass("java/nio/ByteBuffer");
  if (jclazz == nullptr) {
    // exception occurred accessing class
    return nullptr;
  }
  // TODO: introduce caching?
  static const jmethodID jmid_allocate = env->GetStaticMethodID(
      jclazz, "allocate", "(I)Ljava/nio/ByteBuffer;");
  if (jmid_allocate == nullptr) {
    // exception occurred accessing class, or NoSuchMethodException or OutOfMemoryError
    return nullptr;
  }
  const jobject jbuf = env->CallStaticObjectMethod(
      jclazz, jmid_allocate, static_cast<jint>(capacity));
  if (env->ExceptionCheck()) {
    // exception occurred
    return nullptr;
  }

  // Set buffer data
  if (content != nullptr) {
    static const jmethodID array_mid = env->GetMethodID(jclazz, "array", "()[B");
    assert(array_mid != nullptr);
    SetByteBufferData(env, array_mid, jbuf, content, capacity);
  }

  return jbuf;
}

static inline jobject NewDirectByteBuffer(JNIEnv* env, const size_t capacity, const char* content) {
  bool allocated = false;
  if (content == nullptr) {
    content = new char[capacity];
    allocated = true;
  }
  jobject jbuf = env->NewDirectByteBuffer(const_cast<char*>(content), static_cast<jlong>(capacity));
  if (jbuf == nullptr) {
    // exception occurred
    if (allocated) {
      delete[] static_cast<const char*>(content);
    }
    return nullptr;
  }
  return jbuf;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    getInBuffer
 * Signature: ([BII)Ljava/nio/ByteBuffer;
 */
jobject Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_getInBuffer___3BII
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len) {
  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    delete[] key;
    return nullptr;
  }

  // Mock getting value
  std::string value = GetByteArrayInternal(reinterpret_cast<char*>(key));

  // Cleanup
  delete[] key;

  return NewByteBuffer(env, value.size(), value.c_str());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    getInDirectBuffer
 * Signature: ([BII)Ljava/nio/ByteBuffer;
 */
jobject Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_getInDirectBuffer___3BII
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len) {
  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    delete[] key;
    return nullptr;
  }

  // Mock getting value
  std::string value = GetByteArrayInternal(reinterpret_cast<char*>(key));

  // Cleanup
  delete[] key;

  return NewDirectByteBuffer(env, value.size(), value.c_str());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    getInBuffer
 * Signature: ([BIILjava/nio/ByteBuffer;IIZ)I
 */
jint Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_getInBuffer___3BIILjava_nio_ByteBuffer_2IIZ
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval,
      jint jval_off, jint jval_len) {
   static const int kStatusError = -2;
   static const int kArgumentError = -3;

  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    delete[] key;
    return kArgumentError;
  }

  const jclass jclazz = env->FindClass("java/nio/ByteBuffer");
  if (jclazz == nullptr) {
    // exception occurred accessing class
    return kStatusError;
  }

  // Mock getting value
  std::string cvalue = GetByteArrayInternal(reinterpret_cast<char*>(key));

  static const jmethodID array_mid = env->GetMethodID(jclazz, "array", "()[B");
  assert(array_mid != nullptr);

  // TODO: ensure capacity to be jval_off + jval_len, otherwise allow to copy only capacity - jval_off bytes
  //const size_t length = std::min(len, static_cast<size_t>(jval_len - jval_off));
  SetByteBufferData(env, array_mid, jval, cvalue.c_str(), cvalue.size());
  return static_cast<jint>(cvalue.size());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    getInDirectBuffer
 * Signature: ([BIILjava/nio/ByteBuffer;II)I
 */
jint Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_getInDirectBuffer___3BIILjava_nio_ByteBuffer_2II
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval,
         jint jval_off, jint jval_len) {
   static const int kArgumentError = -3;

  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: ArrayIndexOutOfBoundsException
    delete[] key;
    return kArgumentError;
  }

  char* value = reinterpret_cast<char*>(env->GetDirectBufferAddress(jval));
  if (value == nullptr) {
    std::cerr <<
        "Invalid value argument (argument is not a valid direct ByteBuffer)" << std::endl;
    return kArgumentError;
  }
  if (env->GetDirectBufferCapacity(jval) < (jval_off + jval_len)) {
    std::cerr <<
        "Invalid value argument. Capacity is less than requested region "
        "(offset + length)." << std::endl;
    return kArgumentError;
  }

  // Mock getting value
  std::string cvalue = GetByteArrayInternal(reinterpret_cast<char*>(key));

  return SetDirectByteBufferData(value, jval_len, cvalue.c_str(), cvalue.size());
}

/*
 * Class:     com_evolvedbinary_jnibench_common_bytearray_GetByteArray
 * Method:    getInDirectBuffer
 * Signature: (Ljava/nio/ByteBuffer;IILjava/nio/ByteBuffer;II)I
 */
jint Java_com_evolvedbinary_jnibench_common_bytearray_GetByteArray_getInDirectBuffer__Ljava_nio_ByteBuffer_2IILjava_nio_ByteBuffer_2II
  (JNIEnv *env, jclass, jobject jkey, jint jkey_off, jint jkey_len, jobject jval, jint jval_off, jint jval_len) {
  static const int kArgumentError = -3;

  char* key = reinterpret_cast<char*>(env->GetDirectBufferAddress(jkey));
  if (key == nullptr) {
    std::cerr << "Invalid key argument (argument is not a valid direct ByteBuffer)" << std::endl;
    return kArgumentError;
  }
  if (env->GetDirectBufferCapacity(jkey) < (jkey_off + jkey_len)) {
    std::cerr <<
        "Invalid key argument. Capacity is less than requested region (offset "
        "+ length)." << std::endl;
    return kArgumentError;
  }

  char* value = reinterpret_cast<char*>(env->GetDirectBufferAddress(jval));
  if (value == nullptr) {
    std::cerr <<
        "Invalid value argument (argument is not a valid direct ByteBuffer)" << std::endl;
    return kArgumentError;
  }
  if (env->GetDirectBufferCapacity(jval) < (jval_off + jval_len)) {
    std::cerr <<
        "Invalid value argument. Capacity is less than requested region "
        "(offset + length)." << std::endl;
    return kArgumentError;
  }

  std::string cvalue = GetByteArrayInternal(key);

  const jint cvalue_len = static_cast<jint>(cvalue.size());
  const jint length = std::min(jval_len, cvalue_len);

  memcpy(value, cvalue.c_str(), length);

  return cvalue_len;
}