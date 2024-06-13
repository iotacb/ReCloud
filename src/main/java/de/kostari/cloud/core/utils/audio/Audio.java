package de.kostari.cloud.core.utils.audio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import de.kostari.cloud.core.utils.math.MathUtil;

public class Audio {

    private String filePath;
    private boolean looping;
    private int bufferId;
    private List<Integer> sourceIds;
    private float gain;
    private float pitch;

    public Audio(String filePath) {
        this.filePath = filePath;
        this.gain = 1f;
        this.pitch = 1f;
        this.sourceIds = new ArrayList<>();
    }

    public Audio load() {
        bufferId = AL10.alGenBuffers();
        if (filePath.endsWith(".ogg")) {
            loadOgg();
        } else if (filePath.endsWith(".wav")) {
            loadWav();
        } else {
            throw new IllegalArgumentException("Unsupported audio format: " + filePath);
        }
        return this;
    }

    private void loadOgg() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channels = stack.mallocInt(1);
            IntBuffer sampleRate = stack.mallocInt(1);

            ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(filePath, channels, sampleRate);
            if (rawAudioBuffer == null) {
                throw new RuntimeException("Failed to load audio file: " + filePath + "!");
            }

            int format = AL10.AL_FORMAT_MONO16; // Assume mono format
            AL10.alBufferData(bufferId, format, rawAudioBuffer, sampleRate.get());
        }
    }

    private void loadWav() {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);

            int channels = audioIn.getFormat().getChannels();
            int format = (channels == 1) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
            int sampleRate = (int) audioIn.getFormat().getSampleRate();

            byte[] audioBytes = audioIn.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(audioBytes.length).put(audioBytes);
            buffer.flip();

            AL10.alBufferData(bufferId, format, buffer, sampleRate);

            audioIn.close();
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException("Failed to load .wav file: " + filePath, e);
        }
    }

    private int createSource() {
        int sourceId = AL10.alGenSources();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
        // AL10.alSource3f(sourceId, AL10.AL_POSITION, Window.get().getWidth() / 2,
        // Window.get().getHeight() / 2, 0f);
        AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
        return sourceId;
    }

    public void update() {
        for (int sourceId : sourceIds) {
            AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
        }
    }

    public void setGain(float gain) {
        this.gain = gain;
        update();
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        update();
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
        update();
    }

    public void play(boolean randomizePitch) {
        int sourceId = createSource();
        AL10.alSourcePlay(sourceId);
        if (randomizePitch) {
            setPitch(1f + MathUtil.random(-.1f, .1f));
        }
        sourceIds.add(sourceId);
    }

    public void play() {
        play(false);
    }

    public void stop() {
        for (int sourceId : sourceIds) {
            AL10.alSourceStop(sourceId);
            AL10.alDeleteSources(sourceId);
        }
        sourceIds.clear();
    }

    public void cleanUp() {
        stop();
        AL10.alDeleteBuffers(bufferId);
    }
}
