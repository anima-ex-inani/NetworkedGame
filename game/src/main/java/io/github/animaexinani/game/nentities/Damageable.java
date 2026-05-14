package io.github.animaexinani.game.nentities;

public interface Damageable extends Entity {
    /**
     * Applies damage to this entity
     * 
     * @param damage The amount of damage to apply
     */
    void takeDamage(int damage);

    /**
     * Adds a listener that is notified when this entity takes damage
     * 
     * @param listener The listener to add
     * @return <code>true</code> if the listener was successfully added,
     *         <code>false</code> otherwise
     */
    boolean addDamageTakenListener(DamageTakenEventListener listener);

    /**
     * Removes a listener that is notified when this entity takes damage
     * 
     * @param listener The listener to remove
     * @return <code>true</code> if the listener was successfully removed,
     *         <code>false</code> otherwise
     */
    boolean removeDamageTakenListener(DamageTakenEventListener listener);
}
