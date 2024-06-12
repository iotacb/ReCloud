package de.kostari.cloud.core.utils.audio;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;

import de.kostari.cloud.core.window.Window;

public class Audio {

    private String filePath;
    private boolean looping;

    private int bufferId;
    private int sourceId;

    private float gain;
    private float pitch;

    private boolean playing;

    public Audio(String filePath) {
        this.filePath = filePath;
        this.gain = 1f;
        this.pitch = 1f;
    }

    public Audio load() {
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        IntBuffer sampleRate = BufferUtils.createIntBuffer(1);

        ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(filePath, channels, sampleRate);

        if (rawAudioBuffer == null) {
            throw new RuntimeException("Failed to load audio file: " + filePath + "!");
        }

        int channelsCount = channels.get();
        int sampleRateCount = sampleRate.get();
        int format = -1;

        if (channelsCount == 1) {
            format = AL10.AL_FORMAT_MONO16;
        } else if (channelsCount == 2) {
            format = AL10.AL_FORMAT_STEREO16;
        }

        bufferId = AL10.alGenBuffers();
        AL10.alBufferData(bufferId, format, rawAudioBuffer, sampleRateCount);
        sourceId = AL10.alGenSources();

        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
        AL10.alSource3f(sourceId, AL10.AL_POSITION, Window.get().getWidth()
                / 2,
                Window.get().getHeight() / 2,
                0f);

        AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);

        return this;
    }

    public void update() {
        AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void play() {
        int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_STOPPED) {
            this.playing = false;
        }

        if (!playing) {
            AL10.alSourcePlay(sourceId);
            this.playing = true;
        } else {
            stop();
            AL10.alSourcePlay(sourceId);
            this.playing = true;
        }
    }

    public void stop() {
        if (playing) {
            AL10.alSourceStop(sourceId);
            this.playing = false;
        }
    }

}
