package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlocks;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A placeable plunger which upgrades a surrounded small cogwheel into a plunger wheel. */
public class PlungerBlock extends Block {
    private static final VoxelShape END_ROD_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);

    public PlungerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
                                   CollisionContext context) {
        return END_ROD_SHAPE;
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
            if (direction.getAxis() != axis && !level.getBlockState(cogPos.relative(direction)).is(CBPBlocks.PLUNGER_BLOCK.get())) {
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
        level.setBlock(cogPos, CBPBlocks.PLUNGER_WHEEL.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, axis), Block.UPDATE_ALL);
        level.playSound(null, cogPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 0.85F);
    }
}
