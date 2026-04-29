package io.github.animaexinani.engine.audio;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for playing audio.
 */
public interface AudioSystem extends AutoCloseable {
    /**
     * Binds an audio stream to allow it to be played.
     * @param stream The audio stream to bind.
     * @return A playback object that can be used to control the audio stream.
     */
    @NotNull AudioPlayback bindAudio(@NotNull AudioSource stream);
}
