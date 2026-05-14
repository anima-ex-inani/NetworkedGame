package io.github.animaexinani.game.nentities;

import io.github.animaexinani.game.playfield.ServerPlayfield;

public interface Ship extends LivingEntity, DealsContactDamage, Damageable {
    void fireBullet(ServerPlayfield playfield);
}
