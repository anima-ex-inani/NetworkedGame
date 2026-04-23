package io.github.animaexinani.engine.audio;

/**
 * An interface for playing audio.
 */
public interface AudioSystem {
    /**
     * Binds an audio stream to allow it to be played.
     * @param stream The audio stream to bind.
     * @return A playback object that can be used to control the audio stream.
     */
    AudioPlayback bindAudio(AudioSource stream);
}
