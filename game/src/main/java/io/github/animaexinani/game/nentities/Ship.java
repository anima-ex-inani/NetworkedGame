package io.github.animaexinani.game.nentities;

import java.util.ArrayList;
import java.util.List;

public abstract class Ship implements LivingEntity, Damageable {
    int health;
    int shield;
    int maxHealth = 1000;

    List<DamageTakenEventListener> listeners = new ArrayList<>();

    public int health(){
        return this.health;
    }

    public int maxHealth(){
        return this.maxHealth;
    }

    public int shield(){
        return this.shield;
    }

    protected void health(int hp){
        this.health = hp;
    }

    public void takeDamage(int damage){
        int shieldDamage;
        int healthDamage;

        if (this.shield == 0) {
            shieldDamage = 0;
            healthDamage = StrictMath.min(this.health, damage);
        }
        else if (this.shield >= damage) {
            shieldDamage = damage;
            healthDamage = 0;
        }
        else {
            shieldDamage = this.shield;
            healthDamage = damage - this.shield;
        }

        this.shield -= shieldDamage;
        this.health -= healthDamage;

        boolean lethal = this.health == 0;

        for (DamageTakenEventListener listener : this.listeners) {
            listener.onDamageTaken(this, healthDamage, shieldDamage, lethal);
        }
    }

    public boolean addDamageTakenListener(DamageTakenEventListener listener){
        return this.listeners.add(listener);
    }

    public boolean removeDamageTakenListener(DamageTakenEventListener listener){
        return this.listeners.remove(listener);
    }

}
