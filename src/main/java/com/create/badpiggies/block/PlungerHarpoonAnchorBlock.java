package com.create.badpiggies.block;

import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.entity.PlungerHarpoonAnchorBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** The solid plunger left behind by a harpoon; it supplies the rope-coupling endpoint. */
public class PlungerHarpoonAnchorBlock extends Block implements IBE<PlungerHarpoonAnchorBlockEntity> {
    public PlungerHarpoonAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public Class<PlungerHarpoonAnchorBlockEntity> getBlockEntityClass() {
        return PlungerHarpoonAnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PlungerHarpoonAnchorBlockEntity> getBlockEntityType() {
        return CreateBadPiggies.PLUNGER_HARPOON_ANCHOR_ENTITY.get();
    }
}
