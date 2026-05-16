package io.github.animaexinani.game.nentities;

import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.game.util.PhysicsBodyScreenWrap;

/**
 * Entity whose position is wrapped to the playfield each simulation step.
 */
public interface ScreenWrappable extends Entity {

    default void wrapToScreen(@NotNull SizeF bounds) {
        PhysicsBodyScreenWrap.wrap(this.physicsBody(), bounds);
    }
}
