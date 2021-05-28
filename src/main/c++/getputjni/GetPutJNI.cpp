/**
 * Copyright © 2021, Evolved Binary Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#include <jni.h>

#include <string>
#include <cstring>
#include <iostream>

#include "com_evolvedbinary_jnibench_common_getputjni_GetPutJNI.h"

/*
 * Turn these into header and factor methods (at least) out from GetByteArray.cpp
 */
extern const std::string &GetByteArrayInternal(const char *key);
extern char *GetByteArrayInternalForWrite(const char *key, size_t len);

extern jclass g_jbyte_buffer_clazz;
extern jmethodID g_jbyte_buffer_array_mid;
extern jmethodID g_jbyte_buffer_allocate_mid;

//
// Common shortcut code for reading the value from the "fake database"
//
static const char *GetKey(JNIEnv *env, jbyteArray jkey, jint jkey_off, jint jkey_len)
{

  jbyte *key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck())
  {
    // exception thrown: OutOfMemoryError
    delete[] key;
    return nullptr;
  }

  return reinterpret_cast<char *>(key);
}

static const int kError = -1;

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoDirectByteBufferAllocate
 * Signature: ([BIII)Ljava/nio/ByteBuffer;
 */
jobject Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoDirectByteBufferAllocate(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return nullptr;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  return nullptr;

  //**** TODO ALLOCATION IN C++ *******
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoDirectByteBufferFromUnsafe
 * Signature: ([BIIJI)Ljava/nio/ByteBuffer;
 */
jobject Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoDirectByteBufferFromUnsafe(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jlong jval_unsafe_handle, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return nullptr;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  void *buffer_memory = reinterpret_cast<void *>(jval_unsafe_handle);
  jobject byte_buffer = env->NewDirectByteBuffer(buffer_memory, jval_len);
  if (byte_buffer != nullptr)
  {
    memcpy(buffer_memory, cvalue.c_str(), std::min(static_cast<size_t>(jval_len), cvalue.size()));
  }
  return byte_buffer;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoUnsafe
 * Signature: ([BIIJI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoUnsafe(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jlong jval_unsafe_handle, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  void *buffer_memory = reinterpret_cast<void *>(jval_unsafe_handle);
  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  memcpy(buffer_memory, cvalue.c_str(), get_size);

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromUnsafe
 * Signature: ([BIIJI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromUnsafe(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jlong jval_unsafe_handle, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  void *buffer_memory = reinterpret_cast<void *>(jval_unsafe_handle);
  memcpy(db_buf, buffer_memory, jval_len);

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_GetPutJNI
 * Method:    getIntoDirectByteBuffer
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoDirectByteBuffer(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  char *byte_buffer = reinterpret_cast<char *>(env->GetDirectBufferAddress(jval_byte_buffer));
  if (byte_buffer == nullptr)
  {
    std::cerr << "Invalid value argument (argument is not a valid direct ByteBuffer)" << std::endl;
    return kError;
  }
  if (env->GetDirectBufferCapacity(jval_byte_buffer) < jval_len)
  {
    std::cerr << "Invalid value argument. Byte buffer capacity is less than requested length." << std::endl;
    return kError;
  }

  memcpy(byte_buffer, cvalue.c_str(), jval_len);

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_GetPutJNI
 * Method:    putFromDirectByteBuffer
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromDirectByteBuffer(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  char *byte_buffer = reinterpret_cast<char *>(env->GetDirectBufferAddress(jval_byte_buffer));
  if (byte_buffer == nullptr)
  {
    std::cerr << "Invalid value argument (argument is not a valid direct ByteBuffer)" << std::endl;
    return kError;
  }
  if (env->GetDirectBufferCapacity(jval_byte_buffer) < jval_len)
  {
    std::cerr << "Invalid value argument. Byte buffer capacity is less than requested length." << std::endl;
    return kError;
  }

  memcpy(db_buf, byte_buffer, jval_len);

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoByteArraySetRegion
 * Signature: ([BII[BI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoByteArraySetRegion(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval_byte_array, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  env->SetByteArrayRegion(jval_byte_array, 0, get_size, const_cast<jbyte *>(reinterpret_cast<const jbyte *>(cvalue.c_str())));

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromByteArrayGetRegion
 * Signature: ([BII[BI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromByteArrayGetRegion(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval_byte_array, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  env->GetByteArrayRegion(jval_byte_array, 0, jval_len, const_cast<jbyte *>(reinterpret_cast<const jbyte *>(db_buf)));

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoByteArrayGetElements
 * Signature: ([BII[BI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoByteArrayGetElements(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval_byte_array, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  jboolean is_copy;
  jbyte *array_elements = env->GetByteArrayElements(jval_byte_array, &is_copy);
  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  memcpy(array_elements, cvalue.c_str(), get_size);
  env->ReleaseByteArrayElements(jval_byte_array, array_elements, JNI_ABORT);

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromByteArrayGetElements
 * Signature: ([BII[BI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromByteArrayGetElements(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval_byte_array, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  jboolean is_copy;
  jbyte *array_elements = env->GetByteArrayElements(jval_byte_array, &is_copy);
  memcpy(db_buf, array_elements, jval_len);
  env->ReleaseByteArrayElements(jval_byte_array, array_elements, JNI_ABORT);

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoByteArrayCritical
 * Signature: ([BII[BI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoByteArrayCritical(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval_byte_array, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  jboolean is_copy;
  void *array_elements = env->GetPrimitiveArrayCritical(jval_byte_array, &is_copy);
  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  memcpy(array_elements, cvalue.c_str(), get_size);
  env->ReleasePrimitiveArrayCritical(jval_byte_array, array_elements, JNI_ABORT);

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromByteArrayCritical
 * Signature: ([BII[BI)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromByteArrayCritical(JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jbyteArray jval_byte_array, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  jboolean is_copy;
  void *array_elements = env->GetPrimitiveArrayCritical(jval_byte_array, &is_copy);
  memcpy(db_buf, array_elements, jval_len);
  env->ReleasePrimitiveArrayCritical(jval_byte_array, array_elements, JNI_ABORT);

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoIndirectByteBuffer
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoIndirectByteBufferSetRegion(
    JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  jbyteArray buffer_internal_byte_array = static_cast<jbyteArray>(env->CallObjectMethod(jval_byte_buffer, g_jbyte_buffer_array_mid));
  if (env->ExceptionCheck())
  {
    std::cerr << "Invalid call to object method. Byte buffer array." << std::endl;
    return kError;
  }

  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  env->SetByteArrayRegion(buffer_internal_byte_array, 0, get_size, const_cast<jbyte *>(reinterpret_cast<const jbyte *>(cvalue.c_str())));

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromIndirectByteBufferGetRegion
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromIndirectByteBufferGetRegion(
    JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  jbyteArray buffer_internal_byte_array = static_cast<jbyteArray>(env->CallObjectMethod(jval_byte_buffer, g_jbyte_buffer_array_mid));
  if (env->ExceptionCheck())
  {
    std::cerr << "Invalid call to object method. Byte buffer array." << std::endl;
    return kError;
  }

  env->GetByteArrayRegion(buffer_internal_byte_array, 0, jval_len, reinterpret_cast<jbyte *>(db_buf));

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoIndirectByteBufferGetElements
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoIndirectByteBufferGetElements(
    JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  jbyteArray buffer_internal_byte_array = static_cast<jbyteArray>(env->CallObjectMethod(jval_byte_buffer, g_jbyte_buffer_array_mid));
  if (env->ExceptionCheck())
  {
    std::cerr << "Invalid call to object method. Byte buffer array." << std::endl;
    return kError;
  }

  jboolean is_copy;
  jbyte *array_elements = env->GetByteArrayElements(buffer_internal_byte_array, &is_copy);
  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  memcpy(array_elements, cvalue.c_str(), get_size);
  env->ReleaseByteArrayElements(buffer_internal_byte_array, array_elements, JNI_ABORT);

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromIndirectByteBufferGetElements
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromIndirectByteBufferGetElements(
    JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  jbyteArray buffer_internal_byte_array = static_cast<jbyteArray>(env->CallObjectMethod(jval_byte_buffer, g_jbyte_buffer_array_mid));
  if (env->ExceptionCheck())
  {
    std::cerr << "Invalid call to object method. Byte buffer array." << std::endl;
    return kError;
  }

  jboolean is_copy;
  jbyte *array_elements = env->GetByteArrayElements(buffer_internal_byte_array, &is_copy);
  memcpy(db_buf, array_elements, jval_len);
  env->ReleaseByteArrayElements(buffer_internal_byte_array, array_elements, JNI_ABORT);

  return jval_len;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoIndirectByteBufferGetCritical
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoIndirectByteBufferGetCritical(
    JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  jbyteArray buffer_internal_byte_array = static_cast<jbyteArray>(env->CallObjectMethod(jval_byte_buffer, g_jbyte_buffer_array_mid));
  if (env->ExceptionCheck())
  {
    std::cerr << "Invalid call to object method. Byte buffer array." << std::endl;
    return kError;
  }

  jboolean is_copy;
  void *array_elements = env->GetPrimitiveArrayCritical(buffer_internal_byte_array, &is_copy);
  size_t get_size = std::min(static_cast<size_t>(jval_len), cvalue.size());
  memcpy(array_elements, cvalue.c_str(), get_size);
  env->ReleasePrimitiveArrayCritical(buffer_internal_byte_array, array_elements, JNI_ABORT);

  return get_size;
}

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    putFromIndirectByteBufferGetCritical
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_putFromIndirectByteBufferGetCritical(
    JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr)
  {
    return kError;
  }
  char *db_buf = GetByteArrayInternalForWrite(key, jval_len);
  delete[] key;

  jbyteArray buffer_internal_byte_array = static_cast<jbyteArray>(env->CallObjectMethod(jval_byte_buffer, g_jbyte_buffer_array_mid));
  if (env->ExceptionCheck())
  {
    std::cerr << "Invalid call to object method. Byte buffer array." << std::endl;
    return kError;
  }

  jboolean is_copy;
  void *array_elements = env->GetPrimitiveArrayCritical(buffer_internal_byte_array, &is_copy);
  memcpy(db_buf, array_elements, jval_len);
  env->ReleasePrimitiveArrayCritical(buffer_internal_byte_array, array_elements, JNI_ABORT);

  return jval_len;
}
