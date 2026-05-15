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
     * 
     * @implSpec This must return a value greater than or equal to 0. Additionally,
     *           this value should be greater than or equal to the value returned by
     *           {@link #minimumContactDamage()}.
     */
    int contactDamage();

    /**
     * Returns the minimum damage that can be dealt when this entity comes into
     * contact with
     * another entity.
     * 
     * This is used to ensure that even with low impulse, some damage is dealt.
     * 
     * @return the minimum damage amount
     * 
     * @implSpec This must return a value greater than or equal to 0.
     */
    int minimumContactDamage();

    /**
     * Returns the multiplier for the damage dealt when this entity comes into
     * contact with
     * another entity.
     * 
     * @param impulse the impulse of the contact
     * @return the damage multiplier
     * 
     * @implSpec This must return a value greater than or equal to 0.
     */
    float contactDamageMultiplier(double impulse);

    /**
     * Checks if this entity should deal contact damage to the given entity.
     * 
     * @param target the entity to check
     * @return true if this entity should deal contact damage to the given entity,
     *         false otherwise
     */
    boolean dealsContactDamageTo(Damageable target);

    /**
     * Adds a contact damage dealt event listener to this entity.
     * 
     * @param listener the listener to add
     * @return true if the collection of listeners was modified, false otherwise
     */
    boolean addContactDamageDealtListener(ContactDamageDealtEventListener listener);

    /**
     * Removes a contact damage dealt event listener from this entity.
     * 
     * @param listener the listener to remove
     * @return true if the collection of listeners was modified, false otherwise
     */
    boolean removeContactDamageDealtListener(ContactDamageDealtEventListener listener);

    /**
     * Calls all contact damage dealt event listeners with the given parameters.
     * 
     * @param target  the target entity
     * @param damage  the damage amount
     * @param lethal  whether the damage was lethal
     * @param impulse the impulse of the contact
     */
    void callContactDamageDealtListeners(Damageable target, int damage, boolean lethal, double impulse);
}
