package io.github.animaexinani.game.nentities;

import java.util.EventListener;

/**
 * An event listener for when contact damage is dealt to a target.
 */
@FunctionalInterface
public interface ContactDamageDealtEventListener extends EventListener {
    /**
     * Called when contact damage is dealt to a target.
     * 
     * @param source the entity that dealt the damage
     * @param target the entity that received the damage
     * @param damage the amount of damage dealt
     * @param lethal whether the damage was lethal
     * @param impulse the impulse applied to the target
     */
    void onContactDamageDealt(Entity source, Entity target, int damage, boolean lethal, double impulse);
}
