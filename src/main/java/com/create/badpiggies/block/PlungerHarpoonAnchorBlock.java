package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlockEntities;
import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.entity.PlungerHarpoonAnchorBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The solid plunger left behind by a harpoon; it supplies the rope-coupling endpoint. */
public class PlungerHarpoonAnchorBlock extends Block implements IBE<PlungerHarpoonAnchorBlockEntity> {
    private static final VoxelShape END_ROD_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);

    public PlungerHarpoonAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return END_ROD_SHAPE;
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
