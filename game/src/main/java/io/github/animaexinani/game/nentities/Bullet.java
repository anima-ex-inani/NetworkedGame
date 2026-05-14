package io.github.animaexinani.game.nentities;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a bullet within the game's playfield
 */
public interface Bullet extends DealsContactDamage {
    /**
     * Gets the owner of this bullet
     * 
     * @return The bullet's owner, or null if the bullet has no owner
     */
    @Nullable Entity owner();
}
