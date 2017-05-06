package ru.mikroacse.rolespell.app.model.game.entities.components.ai.behaviors;

import ru.mikroacse.engine.util.IntVector2;
import ru.mikroacse.engine.util.Priority;
import ru.mikroacse.engine.util.Timer;
import ru.mikroacse.rolespell.app.model.game.entities.components.movement.MovementComponent;
import ru.mikroacse.rolespell.app.model.game.entities.components.movement.PathMovementComponent;
import ru.mikroacse.rolespell.app.model.game.entities.core.Entity;
import ru.mikroacse.rolespell.app.model.game.world.World;

import java.util.List;

/**
 * Move away from a target.
 */
public class FleeBehavior extends Behavior {
    private int fleeDistance;

    public FleeBehavior(Priority priority, Timer timer, int deactivationDistance) {
        super(priority, false, Trigger.ALL);

        setTimer(timer);
        setDeactivationDistance(deactivationDistance);

        // TODO: magic number
        fleeDistance = 5;
    }

    @Override
    public boolean process(Entity entity, List<Entity> targets) {
        targets.remove(entity);
        if (targets.isEmpty()) {
            return false;
        }

        // get centroid of target positions
        IntVector2 destination = new IntVector2(0, 0);

        int targetCount = 0;

        for (Entity target : targets) {
            if (isTargetActivated(entity, target)) {
                MovementComponent targetMovement = target.getComponent(MovementComponent.class);

                destination.translate(targetMovement.getPosition());

                targetCount++;
            }
        }

        if (targetCount == 0) {
            return false;
        }

        destination.multiply(1 / targetCount);

        // trying to move away in opposite direction

        World world = entity.getWorld();
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        IntVector2 position = movement.getPosition();
        List<IntVector2> passableCells;

        destination = moveAway(destination, position, -5);

        // TODO: magic numbers
        passableCells = world.getPassableCells(
                destination.x,
                destination.y,
                true,
                1,
                15,
                false
        );

        // if entity can move in opposite direction (no wall / location corner)
        if (!passableCells.isEmpty() && tryRouteTo(entity, passableCells)) {
            return true;
        }

        // TODO: magic numbers
        passableCells = world.getPassableCells(
                position.x,
                position.y,
                true,
                1,
                fleeDistance,
                true
        );

        if (passableCells.isEmpty()) {
            return false;
        }

        return tryRouteTo(entity, passableCells);
    }

    private boolean tryRouteTo(Entity entity, List<IntVector2> positions) {
        // TODO: magic numbers

        return entity
                .getComponent(PathMovementComponent.class)
                .tryRouteTo(
                        positions,
                        getPriority(),
                        5,
                        15
                );
    }

    private IntVector2 moveAway(IntVector2 position, IntVector2 origin, int distance) {
        double x2 = position.x - origin.x;
        double y2 = position.y - origin.y;
        double angle = Math.atan2(y2, x2);

        double x1 = Math.cos(angle) * distance;
        double y1 = Math.sin(angle) * distance;

        return new IntVector2(position.x + (int) (x1 - x2), position.y + (int) (y1 - y2));
    }
}
