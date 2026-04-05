package io.github.animaexinani.engine.assets;

public interface Asset {
    /**
     * Checks if the asset is still valid.
     * @return <code>true</code> if the asset can still be used, <code>false</code> otherwise.
     *
     * @implNote
     * This method is used to determine if the asset should be reloaded if it was cached.
     */
    boolean isValid();
}
