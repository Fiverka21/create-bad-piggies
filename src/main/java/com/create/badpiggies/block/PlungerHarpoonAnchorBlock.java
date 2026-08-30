package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlockEntities;
import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.entity.PlungerHarpoonAnchorBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The solid plunger left behind by a harpoon; it supplies the rope-coupling endpoint. */
public class PlungerHarpoonAnchorBlock extends DirectionalBlock implements IBE<PlungerHarpoonAnchorBlockEntity> {
    private static final VoxelShape END_ROD_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
    public static final MapCodec<PlungerHarpoonAnchorBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, PlungerHarpoonAnchorBlock::new));

    public PlungerHarpoonAnchorBlock(BlockBehaviour.Properties properties) {
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
    public Class<PlungerHarpoonAnchorBlockEntity> getBlockEntityClass() {
        return PlungerHarpoonAnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PlungerHarpoonAnchorBlockEntity> getBlockEntityType() {
        return CBPBlockEntities.PLUNGER_HARPOON_ANCHOR_BLOCK_ENTITY.get();
    }
}
