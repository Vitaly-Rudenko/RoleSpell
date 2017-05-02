package ru.mikroacse.rolespell.app.model.game.entities;

import ru.mikroacse.rolespell.app.model.game.entities.components.ai.AttackAi;
import ru.mikroacse.rolespell.app.model.game.entities.components.ai.BehaviorAi;
import ru.mikroacse.rolespell.app.model.game.entities.components.ai.CollisionAvoidingAi;
import ru.mikroacse.rolespell.app.model.game.entities.components.ai.behaviors.Behavior;
import ru.mikroacse.rolespell.app.model.game.entities.components.ai.behaviors.FleeBehavior;
import ru.mikroacse.rolespell.app.model.game.entities.components.movement.PathMovementComponent;
import ru.mikroacse.rolespell.app.model.game.entities.components.status.StatusComponent;
import ru.mikroacse.rolespell.app.model.game.entities.components.status.parameters.DamageParameter;
import ru.mikroacse.rolespell.app.model.game.entities.components.status.parameters.HealthParameter;
import ru.mikroacse.rolespell.app.model.game.entities.core.Entity;
import ru.mikroacse.rolespell.app.model.game.world.World;
import ru.mikroacse.engine.util.Interval;
import ru.mikroacse.engine.util.LimitedDouble;
import ru.mikroacse.engine.util.Priority;

import java.util.EnumSet;

/**
 * Created by MikroAcse on 25.03.2017.
 */
public class Npc extends Entity {
    private CollisionAvoidingAi collisionAvoidingAi;
    private BehaviorAi<Behavior> movementAi;
    private AttackAi attackAi;
    
    private PathMovementComponent movement;
    private StatusComponent status;
    
    public Npc(World world, int x, int y) {
        super(EntityType.NPC, world);
        
        status = new StatusComponent(this);
        
        status.addParameter(new HealthParameter(status));
        
        // TODO: this is bad
        status.addParameter(new DamageParameter(
                status,
                new LimitedDouble(3.0, 10.0),
                1,
                true) {
            @Override
            public boolean bump(Entity entity) {
                if (entity.getType() == EntityType.PLAYER) {
                    return super.bump(entity);
                }
                return false;
            }
        });
        
        addComponent(status);
        
        // TODO: magic numbers everywhere
        
        movement = new PathMovementComponent(this, x, y, 4f);
        movement.setType(PathMovementComponent.UpdateType.POSITION);
        addComponent(movement);
        
        collisionAvoidingAi = new CollisionAvoidingAi(this, 1, 2, false);
        addComponent(collisionAvoidingAi);
        
        movementAi = new BehaviorAi<>(this, 20);
        addComponent(movementAi);
        
        movementAi.setTargetSelectors(EnumSet.of(BehaviorAi.TargetSelector.CUSTOM));

        /*movementAi.addBehavior(
                new WanderBehavior(
                        Priority.LOW,
                        WanderBehavior.Guide.POSITION,
                        2,
                        5,
                        new Interval(new LimitedDouble(4.0, 14.0), true)
                )
        );

        movementAi.addBehavior(
                new SeekBehavior(
                        Priority.NORMAL,
                        new Interval(2.0),
                        5
                )
        );*/
        
        movementAi.addBehavior(
                new FleeBehavior(
                        Priority.NORMAL,
                        new Interval(0.2),
                        5
                )
        );
        
        attackAi = new AttackAi(this, new Interval(new LimitedDouble(3.0, 7.0), true));
        addComponent(attackAi);
    }
    
    public Npc(World world) {
        this(world, 0, 0);
    }
    
    @Override
    public void dispose() {
        movementAi.dispose();
        collisionAvoidingAi.dispose();
        
        movement.dispose();
        status.dispose();
    }
}
