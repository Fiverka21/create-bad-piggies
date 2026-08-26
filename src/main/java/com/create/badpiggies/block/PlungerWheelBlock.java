package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlockEntities;
import com.create.badpiggies.CBPBlocks;
import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.entity.PlungerWheelBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** A small cogwheel wrapped in four plungers, usable as an Offroad wheel-mount tire. */
public class PlungerWheelBlock extends RotatedPillarKineticBlock implements IBE<PlungerWheelBlockEntity>, BlockWithSubLevelCollisionCallback {
    private static final BlockSubLevelCollisionCallback CONTACT_CALLBACK = (wheelPos, surfacePos, normal, depth) -> {
        PlungerWheelBlockEntity.recordSurfaceContact(wheelPos, normal);
        return BlockSubLevelCollisionCallback.CollisionResult.NONE;
    };

    public PlungerWheelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public Class<PlungerWheelBlockEntity> getBlockEntityClass() {
        return PlungerWheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PlungerWheelBlockEntity> getBlockEntityType() {
        return CBPBlockEntities.PLUNGER_WHEEL_BLOCK_ENTITY.get();
    }

    @Override
    public BlockSubLevelCollisionCallback sable$getCallback() {
        return CONTACT_CALLBACK;
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state,
                                   net.minecraft.core.Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public net.minecraft.core.Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public void playerDestroy(Level level, net.minecraft.world.entity.player.Player player, BlockPos pos,
                              BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                              ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide) {
            Block.popResource(level, pos, CBPBlocks.PLUNGER_WHEEL.asItem().getDefaultInstance());
        }
    }
}
