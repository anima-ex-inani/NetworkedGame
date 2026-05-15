package io.github.animaexinani.game.nentities;

public interface Damageable extends Entity {
    /**
     * Applies damage to this entity
     * 
     * @param damage The amount of damage to apply
     * @return <code>true</code> if the entity took lethal damage,
     *         <code>false</code> otherwise
     * 
     * @implSpec It is possible for damage to be negative. The behavior in this case
     *           is implementation-defined.
     * @implSpec Damage taken listeners should NOT be notified if the entity takes
     *           non-positive damage.
     */
    boolean takeDamage(int damage);

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
