/*
 * JOpus, Opus Codec Wrapper for Java
 *
 * (C) 2018, AlaskanEmily, Transnat Games
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

#include "JOpus.h"

#include <opus/opus.h>

/*****************************************************************************/

jlong JNICALL Java_net_alaskanemily_jopus_JOpus_Construct(JNIEnv *env,
    jclass,
    jint sampleRate,
    jint numChannels){
    
    OpusDecoder *const decoder = malloc(opus_decoder_get_size());
    if(opus_decoder_init(decoder, sampeRate, numChannels) != OPUS_OK){
        free(decoder);
        return 0;
    }
    else{
        return (jlong)decoder;
    }
}

/*****************************************************************************/

jobject JNICALL Java_net_alaskanemily_jopus_JOpus_decode(JNIEnv *env,
    jclass,
    jlong ptr,
    jbyteArray dataArray,
    jint num_channels,
    jclass shortBufferClass){
    
    const unsigned maxSamples = 5760;
    const unsigned arrayCount = maxSamples * numChannels;
    const jshortArray shortArray = NewShortArray(env, arrayCount);
    
    unsigned decoded_size;
    {
        OpusDecoder *const decoder = (OpusDecoder*)ptr;
        const unsigned dataArrayLength = GetArrayLength(env, dataArray);
        short *outData = GetPrimitiveArrayCritical(env, shortArray, NULL);
        const void *inData = GetPrimitiveArrayCritical(env, dataArray, NULL);
        
        decoded_size = opus_decode(decoder,
            inData,
            dataArrayLength,
            outData,
            maxSamples,
            0);
        
        ReleasePrimitiveArrayCritical(env, dataArray, inData, JNI_ABORT);
        ReleasePrimitiveArrayCritical(env, shortArray, outData, 0);
    }
    
    {
        const jmethodId wrapMethod = GetStaticMethodID(env,
            shortBufferClass,
            "wrap",
            "([SII)Ljava.nio.ShortBuffer;");
        
        jvalue args[3];
        
        args[0].l = shortArray;
        args[1].i = 0;
        args[2].i = decoded_size;
        
        return CallStaticObjectMethodA(env,
            shortBufferClass,
            wrapMethod,
            args);
    }
}

/*****************************************************************************/

jobject JNICALL Java_net_alaskanemily_jopus_JOpus_decodeFloat(JNIEnv *env,
    jclass,
    jlong ptr,
    jbyteArray dataArray,
    jint numChannels,
    jclass floatBufferClass){
    
    const unsigned maxSamples = 5760;
    const unsigned arrayCount = maxSamples * numChannels;
    const jfloatArray floatArray = NewFloatArray(env, arrayCount);
    
    unsigned decoded_size;
    {
        OpusDecoder *const decoder = (OpusDecoder*)ptr;
        const unsigned dataArrayLength = GetArrayLength(env, dataArray);
        float *outData = GetPrimitiveArrayCritical(env, floatArray, NULL);
        const void *inData = GetPrimitiveArrayCritical(env, dataArray, NULL);
        
        decoded_size = opus_decode_float(decoder,
            inData,
            dataArrayLength,
            outData,
            maxSamples,
            0);
        
        ReleasePrimitiveArrayCritical(env, dataArray, inData, JNI_ABORT);
        ReleasePrimitiveArrayCritical(env, floatArray, outData, 0);
    }
    
    {
        const jmethodId wrapMethod = GetStaticMethodID(env,
            floatBufferClass,
            "wrap",
            "([FII)Ljava.nio.FloatBuffer;");
        
        jvalue args[3];
        
        args[0].l = floatArray;
        args[1].i = 0;
        args[2].i = decoded_size;
        
        return CallStaticObjectMethodA(env,
            floatBufferClass,
            wrapMethod,
            args);
    }
}

/*****************************************************************************/

void JNICALL Java_net_alaskanemily_jopus_JOpus_destroy(JNIEnv *,
    jclass,
    jlong ptr){
    
    OpusDecoder *const decoder = (OpusDecoder*)ptr;
    opus_decoder_destroy(decoder);
    free(decoder);
}
