package com.create.badpiggies.block.entity;

import com.create.badpiggies.block.AdjustableTntBlock;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Supplies the adjustable TNT charge count to Create's Engineer's Goggles overlay. */
public class AdjustableTntBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    public AdjustableTntBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int gunpowder = getBlockState().getValue(AdjustableTntBlock.GUNPOWDER);
        tooltip.add(Component.translatable("goggle.createbadpiggies.adjustable_tnt.gunpowder",
                gunpowder, 10).withStyle(ChatFormatting.GOLD));
        return true;
    }
}
