package io.github.animaexinani.game.nentities;

import java.util.ArrayList;
import java.util.List;

public abstract class Asteroid implements Entity, Damageable {
    int health = 500_000;
    List<DamageTakenEventListener> listeners = new ArrayList<>();

    public void takeDamage(int damage){
        if (damage > this.health) {
            this.health = 0;
            for (var listener : this.listeners){
                listener.onDamageTaken(this, this.health, 0, true);
            }
        }else{
            this.health -= damage;
            for (var listener : this.listeners){
                listener.onDamageTaken(this, damage, 0, false);
            }
        }
    }

    public boolean addDamageTakenListener(DamageTakenEventListener listener){
        return this.listeners.add(listener);
    }

    public boolean removeDamageTakenListener(DamageTakenEventListener listener){
        return this.listeners.remove(listener);
    }
}
