package com.create.badpiggies.block.entity;

import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.PlungerWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelReactionWheel;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Applies a short, inward impulse whenever a spinning plunger wheel is in surface contact. */
public class PlungerWheelBlockEntity extends SimpleKineticBlockEntity
        implements BlockEntitySubLevelActor, BlockEntitySubLevelReactionWheel {
    private static final double MINIMUM_RPM = 4.0D;
    private static final double ADHESION_PER_RPM = 0.0025D;
    private static final Map<BlockPos, Vector3d> CONTACT_NORMALS = new ConcurrentHashMap<>();
    private final ForceTotal adhesionForce = new ForceTotal();

    public PlungerWheelBlockEntity(BlockPos pos, BlockState state) {
        super(CreateBadPiggies.PLUNGER_WHEEL_ENTITY.get(), pos, state);
    }

    public static void recordSurfaceContact(BlockPos wheelPos, Vector3d normal) {
        CONTACT_NORMALS.put(wheelPos.immutable(), new Vector3d(normal));
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle body, double deltaSeconds) {
        Vector3d normal = CONTACT_NORMALS.remove(worldPosition);
        double rpm = Math.abs(getSpeed());
        if (normal == null || rpm < MINIMUM_RPM || normal.lengthSquared() < 1.0E-6D) {
            return;
        }

        // The contact normal points away from the contacted surface; pulling against it keeps the rim seated.
        normal.normalize().mul(-Math.min(0.6D, rpm * ADHESION_PER_RPM) * deltaSeconds);
        adhesionForce.applyImpulseAtPoint(subLevel, new Vector3d(worldPosition.getX() + .5D,
                worldPosition.getY() + .5D, worldPosition.getZ() + .5D), normal);
        body.applyForcesAndReset(adhesionForce);
    }

    @Override
    public void sable$getAngularVelocity(Vector3d angularVelocity) {
        Direction.Axis axis = getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS);
        double radiansPerTick = getSpeed() * Math.PI / 1800.0D;
        angularVelocity.set(axis == Direction.Axis.X ? radiansPerTick : 0.0D,
                axis == Direction.Axis.Y ? radiansPerTick : 0.0D,
                axis == Direction.Axis.Z ? radiansPerTick : 0.0D);
    }
}
