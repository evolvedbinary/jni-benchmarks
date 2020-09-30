#include <jni.h>

#include <cstring>

extern jclass g_jbyte_buffer_clazz;
extern jmethodID g_jbyte_buffer_array_mid;
extern jmethodID g_jbyte_buffer_allocate_mid;

inline jbyteArray StringToJavaByteArray(JNIEnv *env, const std::string& str) {
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

inline void SetByteBufferData(JNIEnv* env, const jmethodID jarray_mid, const jobject& jbuf,
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

  env->ReleasePrimitiveArrayCritical(jarray, ja, is_copy ? 0 : JNI_ABORT);

  env->DeleteLocalRef(jarray);
}

inline jobject NewByteBuffer(JNIEnv* env, const size_t capacity, const char* content) {

  const jobject jbuf = env->CallStaticObjectMethod(
      g_jbyte_buffer_clazz, g_jbyte_buffer_allocate_mid, static_cast<jint>(capacity));
  if (env->ExceptionCheck()) {
    // exception occurred
    return nullptr;
  }

  // Set buffer data
  if (content != nullptr) {
    SetByteBufferData(env, g_jbyte_buffer_array_mid, jbuf, content, capacity);
  }

  return jbuf;
}

inline jobject NewDirectByteBuffer(JNIEnv* env, const size_t capacity, const char* content) {
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