package com.create.badpiggies.block.entity;

import com.create.badpiggies.CreateBadPiggies;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** A rope-coupling holder attached to the solid plunger placed by a harpoon impact. */
public class PlungerHarpoonAnchorBlockEntity extends SmartBlockEntity implements RopeStrandHolderBlockEntity {
    public PlungerHarpoonAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(CreateBadPiggies.PLUNGER_HARPOON_ANCHOR_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new RopeStrandHolderBehavior(this));
    }

    @Override
    public RopeStrandHolderBehavior getBehavior() {
        return getBehaviour(RopeStrandHolderBehavior.TYPE);
    }

    @Override
    public Vec3 getAttachmentPoint(BlockPos pos, BlockState state) {
        return Vec3.atCenterOf(pos);
    }
}
