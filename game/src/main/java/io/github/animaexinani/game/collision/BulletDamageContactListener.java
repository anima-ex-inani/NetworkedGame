package io.github.animaexinani.game.collision;

import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

import io.github.animaexinani.game.nentities.Bullet;
import io.github.animaexinani.game.nentities.Damageable;
import io.github.animaexinani.game.nentities.Entity;

public class BulletDamageContactListener extends ContactListenerAdapter<PhysicsBody> {
    @Override
    public void begin(ContactCollisionData<PhysicsBody> collision, Contact contact) {
        PhysicsBody body1 = collision.getBody1();
        PhysicsBody body2 = collision.getBody2();

        Object userData1 = body1.getUserData();
        Object userData2 = body2.getUserData();

        if (userData1 instanceof Entity entity1 && userData2 instanceof Entity entity2) {
            this.applyDamageIfApplicable(entity1, entity2);
            this.applyDamageIfApplicable(entity2, entity1);
        }
    }

    private void applyDamageIfApplicable(Entity source, Entity target) {
        if (source instanceof Bullet bullet && target instanceof Damageable damageable) {
            if (!bullet.dealsDamageTo(damageable)) {
                return;
            }

            var damage = bullet.damage();
            var actualDamage = StrictMath.max(damage, bullet.minimumDamage());
            boolean lethal = damageable.takeDamage(actualDamage);

            bullet.callDamageDealtListeners(damageable, actualDamage, lethal);
        }
    }

    @Override
    public void collision(ContactCollisionData<PhysicsBody> collision) {
        PhysicsBody body1 = collision.getBody1();
        PhysicsBody body2 = collision.getBody2();

        Object userData1 = body1.getUserData();
        Object userData2 = body2.getUserData();

        if (userData1 instanceof Bullet || userData2 instanceof Bullet) {
            collision.getContactConstraint().setEnabled(false);
        }
    }
}
