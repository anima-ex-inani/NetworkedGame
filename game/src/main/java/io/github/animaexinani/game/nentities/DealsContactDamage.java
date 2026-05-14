package io.github.animaexinani.game.nentities;

/**
 * Represents an entity that deals damage when it comes into contact with
 * another entity.
 */
public interface DealsContactDamage extends Entity {
    /**
     * Returns the amount of damage dealt when this entity comes into contact with
     * another entity.
     * 
     * @return the damage amount
     */
    int contactDamage();

    /**
     * Returns the minimum damage that can be dealt when this entity comes into contact with
     * another entity.
     * 
     * This is used to ensure that even with low impulse, some damage is dealt.
     * 
     * @return the minimum damage amount
     */
    int minimumContactDamage();

    /**
     * Returns the multiplier for the damage dealt when this entity comes into contact with
     * another entity.
     * 
     * @param impulse the impulse of the contact
     * @return the damage multiplier
     */
    float contactDamageMultiplier(double impulse);

    /**
     * Checks if this entity should deal contact damage to the given entity.
     * 
     * @param target the entity to check
     * @return true if this entity should deal contact damage to the given entity, false otherwise
     */
    boolean dealsContactDamageTo(Damageable target);
}
