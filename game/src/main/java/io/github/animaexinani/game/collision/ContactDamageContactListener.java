package io.github.animaexinani.game.collision;

import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.dynamics.contact.SolvedContact;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

import io.github.animaexinani.game.nentities.Damageable;
import io.github.animaexinani.game.nentities.DealsContactDamage;
import io.github.animaexinani.game.nentities.Entity;

public class ContactDamageContactListener extends ContactListenerAdapter<PhysicsBody> {

    @Override
    public void postSolve(ContactCollisionData<PhysicsBody> collision, SolvedContact contact) {
        PhysicsBody body1 = collision.getBody1();
        PhysicsBody body2 = collision.getBody2();

        Object data1 = body1.getUserData();
        Object data2 = body2.getUserData();

        if (data1 instanceof Entity entity1 && data2 instanceof Entity entity2) {
            var impulse = contact.getNormalImpulse();
            this.applyDamageIfApplicable(entity1, entity2, impulse);
            this.applyDamageIfApplicable(entity2, entity1, impulse);
        }
    }

    private void applyDamageIfApplicable(Entity source, Entity target, double impulse) {
        if (source instanceof DealsContactDamage dealer && target instanceof Damageable damageable) {
            if (!dealer.dealsContactDamageTo(damageable)) {
                return;
            }

            var damage = dealer.contactDamage();
            damage *= dealer.contactDamageMultiplier(impulse);
            var actualDamage = StrictMath.max(damage, dealer.minimumContactDamage());
            boolean lethal = damageable.takeDamage(actualDamage);
            dealer.callContactDamageDealtListeners(damageable, actualDamage, lethal, impulse);
        }
    }
}
