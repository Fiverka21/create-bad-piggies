package com.create.badpiggies.block.entity;

import com.create.badpiggies.block.SodaBottleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Stores the soda bottle block on assembled vehicles. */
public class SodaBottleBlockEntity extends BlockEntity {
    public SodaBottleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
