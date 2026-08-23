package com.create.badpiggies.block;

import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.entity.PlungerHarpoonBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A shaft-driven, redstone-triggered launcher which fires loaded plungers. */
public class PlungerHarpoonBlock extends DirectionalAxisKineticBlock implements IBE<PlungerHarpoonBlockEntity> {
    public PlungerHarpoonBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public Class<PlungerHarpoonBlockEntity> getBlockEntityClass() {
        return PlungerHarpoonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PlungerHarpoonBlockEntity> getBlockEntityType() {
        return CreateBadPiggies.PLUNGER_HARPOON_ENTITY.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PlungerHarpoonBlockEntity launcher)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(CreateBadPiggies.PLUNGER.get()) && launcher.load()) {
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (stack.isEmpty() && launcher.unload()) {
            if (!player.getInventory().add(CreateBadPiggies.PLUNGER.get().getDefaultInstance())) {
                player.drop(CreateBadPiggies.PLUNGER.get().getDefaultInstance(), false);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                BlockPos fromPos, boolean moving) {
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            withBlockEntityDo(level, pos, PlungerHarpoonBlockEntity::tryFire);
        }
        super.neighborChanged(state, level, pos, block, fromPos, moving);
    }
}
