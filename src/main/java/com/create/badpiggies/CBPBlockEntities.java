package com.create.badpiggies;

import com.create.badpiggies.block.entity.AdjustableTntBlockEntity;
import com.create.badpiggies.block.entity.PlungerHarpoonAnchorBlockEntity;
import com.create.badpiggies.block.entity.PlungerHarpoonBlockEntity;
import com.create.badpiggies.block.entity.PlungerWheelBlockEntity;
import com.create.badpiggies.block.entity.SodaBottleBlockEntity;
import com.create.badpiggies.client.PlungerHarpoonRopeRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.create.badpiggies.CreateBadPiggies.REGISTRATE;

public class CBPBlockEntities {

    public static final BlockEntityEntry<AdjustableTntBlockEntity> ADJUSTABLE_TNT_BLOCK_ENTITY =
            REGISTRATE.blockEntity("adjustable_tnt", AdjustableTntBlockEntity::new)
                    .validBlock(CBPBlocks.ADJUSTABLE_TNT)
                    .register();

    public static final BlockEntityEntry<PlungerHarpoonBlockEntity> PLUNGER_HARPOON_BLOCK_ENTITY =
            REGISTRATE.blockEntity("plunger_harpoon", PlungerHarpoonBlockEntity::new)
                    .validBlock(CBPBlocks.PLUNGER_HARPOON_BLOCK)
                    .renderer(() -> PlungerHarpoonRopeRenderer::new)
                    .register();

    public static final BlockEntityEntry<PlungerHarpoonAnchorBlockEntity> PLUNGER_HARPOON_ANCHOR_BLOCK_ENTITY =
            REGISTRATE.blockEntity("plunger_harpoon_anchor", PlungerHarpoonAnchorBlockEntity::new)
                    .validBlock(CBPBlocks.PLUNGER_HARPOON_ANCHOR)
                    .register();

    public static final BlockEntityEntry<PlungerWheelBlockEntity> PLUNGER_WHEEL_BLOCK_ENTITY =
            REGISTRATE.blockEntity("plunger_wheel", PlungerWheelBlockEntity::new)
                    .validBlock(CBPBlocks.PLUNGER_WHEEL)
                    .register();

    public static final BlockEntityEntry<SodaBottleBlockEntity> SODA_BOTTLE_BLOCK_ENTITY =
            REGISTRATE.blockEntity("soda_bottle", SodaBottleBlockEntity::new)
                    .validBlock(CBPBlocks.SODA_BOTTLE)
                    .register();

    public static void load() {}
}
