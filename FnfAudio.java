package com.waosfnf.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Reproduce un .ogg externo (carpeta fnf) usando el OpenAL que ya inicia Minecraft. */
public class FnfAudio {
    private int buffer, source;
    public double durationMs;

    private FnfAudio() {}

    public static float volume() {
        var o = Minecraft.getInstance().options;
        return o.getSoundSourceVolume(SoundSource.MASTER) * o.getSoundSourceVolume(SoundSource.MUSIC);
    }

    /** Devuelve null si no se puede cargar (la cancion se juega en silencio). */
    public static FnfAudio load(Path path) {
        if (path == null) return null;
        ByteBuffer data = null;
        ShortBuffer pcm = null;
        try {
            byte[] bytes = Files.readAllBytes(path);
            data = MemoryUtil.memAlloc(bytes.length);
            data.put(bytes);
            data.flip();
            try (MemoryStack st = MemoryStack.stackPush()) {
                IntBuffer ch = st.mallocInt(1);
                IntBuffer sr = st.mallocInt(1);
                pcm = STBVorbis.stb_vorbis_decode_memory(data, ch, sr);
                if (pcm == null) return null;
                int channels = ch.get(0);
                int rate = sr.get(0);
                if (channels > 2) return null;
                FnfAudio a = new FnfAudio();
                a.buffer = AL10.alGenBuffers();
                AL10.alBufferData(a.buffer, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, pcm, rate);
                a.source = AL10.alGenSources();
                AL10.alSourcei(a.source, AL10.AL_BUFFER, a.buffer);
                AL10.alSourcei(a.source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
                AL10.alSource3f(a.source, AL10.AL_POSITION, 0, 0, 0);
                a.durationMs = pcm.remaining() / (double) channels / rate * 1000.0;
                return a;
            }
        } catch (Throwable t) {
            LogUtils.getLogger().warn("[WaosFNF] No se pudo cargar el audio " + path, t);
            return null;
        } finally {
            if (data != null) MemoryUtil.memFree(data);
            if (pcm != null) MemoryUtil.memFree(pcm);
        }
    }

    public void play() {
        AL10.alSourcef(source, AL10.AL_GAIN, volume());
        AL10.alSourcePlay(source);
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public double positionMs() {
        return AL10.alGetSourcef(source, AL10.AL_SEC_OFFSET) * 1000.0;
    }

    public void stop() {
        if (source != 0) { AL10.alSourceStop(source); AL10.alDeleteSources(source); source = 0; }
        if (buffer != 0) { AL10.alDeleteBuffers(buffer); buffer = 0; }
    }
}
