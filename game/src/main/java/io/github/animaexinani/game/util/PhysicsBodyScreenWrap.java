package io.github.animaexinani.game.util;

import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.geometry.Transform;

import io.github.animaexinani.engine.size.SizeF;

/**
 * Wraps a physics body's translation to the opposite edge of a rectangular playfield,
 * matching {@link io.github.animaexinani.game.entities.Entity#wrapPosition}.
 */
public final class PhysicsBodyScreenWrap {

    private PhysicsBodyScreenWrap() {
    }

    public static void wrap(PhysicsBody body, SizeF bounds) {
        Transform t = body.getTransform();
        double x = t.getTranslationX();
        double y = t.getTranslationY();
        double w = bounds.width();
        double h = bounds.height();
        boolean wrapped = false;

        if (x > w) {
            x = 0;
            wrapped = true;
        } else if (x < 0) {
            x = w;
            wrapped = true;
        }

        if (y > h) {
            y = 0;
            wrapped = true;
        } else if (y < 0) {
            y = h;
            wrapped = true;
        }

        if (wrapped) {
            t.setTranslation(x, y);
        }
    }
}
