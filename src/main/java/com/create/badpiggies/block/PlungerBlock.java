package com.create.badpiggies.block;

import com.create.badpiggies.CreateBadPiggies;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** A placeable plunger which upgrades a surrounded small cogwheel into a plunger wheel. */
public class PlungerBlock extends Block {
    public PlungerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos cogPos = pos.relative(direction);
            BlockState cogState = level.getBlockState(cogPos);
            if (!(cogState.getBlock() instanceof CogWheelBlock cogwheel) || !cogwheel.isSmallCog()) {
                continue;
            }
            Direction.Axis axis = cogState.getValue(BlockStateProperties.AXIS);
            if (isCompleteWheel(level, cogPos, axis)) {
                formWheel(level, cogPos, cogState, axis);
                return;
            }
        }
    }

    private static boolean isCompleteWheel(Level level, BlockPos cogPos, Direction.Axis axis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis && !level.getBlockState(cogPos.relative(direction)).is(CreateBadPiggies.PLUNGER_BLOCK.get())) {
                return false;
            }
        }
        return true;
    }

    private static void formWheel(Level level, BlockPos cogPos, BlockState cogState, Direction.Axis axis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != axis) {
                level.removeBlock(cogPos.relative(direction), false);
            }
        }
        level.setBlock(cogPos, CreateBadPiggies.PLUNGER_WHEEL.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, axis), Block.UPDATE_ALL);
        level.playSound(null, cogPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 0.85F);
    }
}
