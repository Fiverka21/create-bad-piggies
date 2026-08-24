package com.create.badpiggies.block;

import com.mojang.serialization.MapCodec;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

// Physics from the sail block
// Needed gemini help for Codex error

public class UmbrellaBlock extends DirectionalBlock implements BlockSubLevelLiftProvider {

    public static final MapCodec<UmbrellaBlock> CODEC = simpleCodec(UmbrellaBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public UmbrellaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(OPEN, true)); // Default to OPEN/end rod when unpowered
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean hasSignal = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(OPEN, !hasSignal); // Unpowered = OPEN (true), Powered = CLOSED (false)
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean shouldBeOpen = !hasSignal;

            if (state.getValue(OPEN) != shouldBeOpen) {
                level.setBlock(pos, state.setValue(OPEN, shouldBeOpen), 3);
            }
        }
    }

    // --- SABLE / SIMULATED PHYSICS INTERFACE ---

    @Override
    public float sable$getParallelDragScalar() {
        // Drag is active when OPEN = true (unpowered / end rod state)
        return 10.0f; // I'll tweak it
    }

    @Override
    public float sable$getLiftScalar() {
        return 0.0f;
    }

    @Override
    public @NotNull Direction sable$getNormal(final BlockState blockState) {
        return blockState.getValue(FACING);
    }
}