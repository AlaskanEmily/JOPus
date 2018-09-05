package net.alaskanemily.jopus;

/*
 * JOpus, Opus Codec Wrapper for Java
 *
 * (C) 2018, AlaskanEmily, Transnat Games
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class JOpus {
    final private long opusDecoder;
    final public int numChannels;
    final public int sampleRate;
    
    final private static int defaultSampleRate = 48000;
    
    /**
     * Signature that Opus-in-Ogg uses to identify an Opus track.
     */
    final private static String signature = "OpusHead";
    
    /**
     * Native constructor which creates an OpusDecoder struct
     */
    private native static long Construct(int sample_rate, int num_channels);
    
    /**
     * Validates that than ogg packet is starting an Opus track.
     */
    public static boolean validate(byte[] first_ogg_packet){
        
        if(first_ogg_packet == null)
            throw new NullPointerException("Ogg packet data was null");
        
        // The packet must be at least 19 bytes long.
        if(first_ogg_packet.length < 19)
            return false;
        
        // Check that the signature matches.
        for(int i = 0; i < signature.length(); i++){
            if(first_ogg_packet[i] != signature.codePointAt(i))
                return false;
        }
        
        // This is the version field. Must be 1.
        if(first_ogg_packet[8] != 1)
            return false;
        
        return true;
    }
    
    /**
     * Validates that than ogg packet is starting an Opus track.
     */
    public static boolean validate(ByteBuffer first_ogg_packet){
        if(first_ogg_packet == null)
            throw new NullPointerException("Ogg packet data was null");
        return validate(first_ogg_packet.array());
    }
    
    /**
     * Gets the number of channels from an Opus-in-Ogg packet.
     */
    public static int decodeNumChannels(byte[] first_ogg_packet)
        throws UnsupportedAudioFileException {
        
        if(first_ogg_packet == null)
            throw new NullPointerException("Ogg packet data was null");
        
        if(!validate(first_ogg_packet))
            throw new UnsupportedAudioFileException("Packet is not Opus audio");
        return first_ogg_packet[9];
    }
    
    /**
     * Gets the number of channels from an Opus-in-Ogg packet.
     */
    public static int decodeNumChannels(ByteBuffer first_ogg_packet)
        throws UnsupportedAudioFileException {
        
        if(first_ogg_packet == null)
            throw new NullPointerException("Ogg packet data was null");
        return decodeNumChannels(first_ogg_packet.array());
    }
    
    public JOpus(ByteBuffer first_ogg_packet)
        throws UnsupportedAudioFileException {
        
        if(!validate(first_ogg_packet))
            throw new UnsupportedAudioFileException("Packet is not Opus audio");
        numChannels = decodeNumChannels(first_ogg_packet);
        opusDecoder = Construct(defaultSampleRate, numChannels);
        if(opusDecoder == 0)
            throw new UnsupportedAudioFileException("Invalid audio format");
        sampleRate = defaultSampleRate;
    }
    
    public JOpus(byte[] first_ogg_packet)
        throws UnsupportedAudioFileException {
        
        if(!validate(first_ogg_packet))
            throw new UnsupportedAudioFileException("Packet is not Opus audio");
        numChannels = decodeNumChannels(first_ogg_packet);
        opusDecoder = Construct(defaultSampleRate, numChannels);
        if(opusDecoder == 0)
            throw new UnsupportedAudioFileException("Invalid audio format");
        sampleRate = defaultSampleRate;
    }
    
    public JOpus(int num_channels, int sample_rate)
        throws UnsupportedAudioFileException {
        
        numChannels = num_channels;
        sampleRate = sample_rate;
        opusDecoder = Construct(sample_rate, num_channels);
        if(opusDecoder == 0)
            throw new UnsupportedAudioFileException("Invalid audio format");
    }
    
    /**
     * Decodes a buffer of Opus data into uint16 PCM data
     */
    public ShortBuffer decode(ByteBuffer data){
        if(data == null)
            throw new NullPointerException("Ogg packet data was null");
        return decode(opusDecoder, data.array(), numChannels, ShortBuffer.class);
    }
    
    public ShortBuffer decode(byte[] data){
        if(data == null)
            throw new NullPointerException("Ogg packet data was null");
        return decode(opusDecoder, data, numChannels, ShortBuffer.class);
    }
    
    /**
     * Decodes a buffer of Opus data into uint16 PCM data
     */
    private static native ShortBuffer decode(long decoder,
        byte[] data,
        int num_channels,
        java.lang.Class<ShortBuffer> clazz);
    
    /**
     * Decodes a buffer of Opus data into 32-bit floating point audio data.
     */
    public FloatBuffer decodeFloat(ByteBuffer data){
        if(data == null)
            throw new NullPointerException("Ogg packet data was null");
        return decodeFloat(opusDecoder, data.array(), numChannels, FloatBuffer.class);
    }
    
    /**
     * Decodes a buffer of Opus data into 32-bit floating point audio data.
     */
    public FloatBuffer decodeFloat(byte[] data){
        if(data == null)
            throw new NullPointerException("Ogg packet data was null");
        return decodeFloat(opusDecoder, data, numChannels, FloatBuffer.class);
    }
    
    private static native FloatBuffer decodeFloat(long decoder,
        byte[] data,
        int numChannels,
        java.lang.Class<FloatBuffer> clazz);
    
    private native static void destroy(long decoder);
    
    protected void finalize() throws Throwable{
        destroy(opusDecoder);
        super.finalize();
    }
}
