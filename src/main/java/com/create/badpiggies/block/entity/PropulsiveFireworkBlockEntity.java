package com.create.badpiggies.block.entity;

import com.create.badpiggies.block.PropulsiveFireworkBlock;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Supplies the firework charge count to Create's Engineer's Goggles overlay. */
public class PropulsiveFireworkBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    public PropulsiveFireworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int gunpowder = getBlockState().getValue(PropulsiveFireworkBlock.GUNPOWDER);
        tooltip.add(Component.translatable("goggle.createbadpiggies.propulsive_firework.gunpowder",
                gunpowder, 10).withStyle(ChatFormatting.GOLD));
        return true;
    }
}
