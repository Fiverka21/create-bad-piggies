package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlocks;
import com.create.badpiggies.CreateBadPiggies;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A placeable plunger which upgrades a surrounded small cogwheel into a plunger wheel. */
public class PlungerBlock extends DirectionalBlock {
    private static final VoxelShape END_ROD_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
    public static final MapCodec<PlungerBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, PlungerBlock::new));

    public PlungerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
                                   CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> Block.box(0.0D, 7.0D, 7.0D, 16.0D, 9.0D, 9.0D);
            case Y -> END_ROD_SHAPE;
            case Z -> Block.box(7.0D, 7.0D, 0.0D, 9.0D, 9.0D, 16.0D);
        };
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
