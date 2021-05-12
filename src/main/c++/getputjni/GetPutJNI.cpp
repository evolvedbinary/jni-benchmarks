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

extern const std::string& GetByteArrayInternal(const char* key);

//
// Common shortcut code for reading the value from the "fake database"
//
static const char *GetKey(JNIEnv *env, jbyteArray jkey, jint jkey_off, jint jkey_len) {
    
  jbyte* key = new jbyte[jkey_len];
  env->GetByteArrayRegion(jkey, jkey_off, jkey_len, key);
  if (env->ExceptionCheck()) {
    // exception thrown: OutOfMemoryError
    delete[] key;
    return nullptr;
  }

  return reinterpret_cast<char*>(key);
}

static const int kError = -1;

/*
 * Class:     com_evolvedbinary_jnibench_common_getputjni_GetPutJNI
 * Method:    getIntoDirectByteBufferAllocate
 * Signature: ([BIII)Ljava/nio/ByteBuffer;
 */
jobject JNICALL Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoDirectByteBufferAllocate
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr) {
      return nullptr;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  return nullptr;

    //**** TODO ALLOCATION IN C++ *******
}

/*
 * Class:     com_evolvedbinary_jnibench_common_GetPutJNI
 * Method:    getIntoDirectByteBuffer
 * Signature: ([BIILjava/nio/ByteBuffer;I)I
 */
jint Java_com_evolvedbinary_jnibench_common_getputjni_GetPutJNI_getIntoDirectByteBuffer
  (JNIEnv *env, jclass, jbyteArray jkey, jint jkey_off, jint jkey_len, jobject jval_byte_buffer, jint jval_len)
{
  const char *key = GetKey(env, jkey, jkey_off, jkey_len);
  if (key == nullptr) {
      return kError;
  }
  std::string cvalue = GetByteArrayInternal(key);
  delete[] key;

  char* byte_buffer = reinterpret_cast<char*>(env->GetDirectBufferAddress(jval_byte_buffer));
  if (byte_buffer == nullptr) {
    std::cerr << "Invalid value argument (argument is not a valid direct ByteBuffer)" << std::endl;
    return kError;
  }
  if (env->GetDirectBufferCapacity(jval_byte_buffer) < jval_len) {
    std::cerr <<
        "Invalid value argument. Byte buffer capacity is less than requested length." << std::endl;
    return kError;
  }

  memcpy(byte_buffer, cvalue.c_str(), jval_len);

  return jval_len;
}

