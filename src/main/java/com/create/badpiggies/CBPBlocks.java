package com.create.badpiggies;

import com.create.badpiggies.block.*;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;

import static com.create.badpiggies.CreateBadPiggies.REGISTRATE;

public class CBPBlocks {
    static {
        REGISTRATE.defaultCreativeTab(CreateBadPiggies.CREATIVE_TAB_KEY);
    }


    public static final BlockEntry<PlungerBlock> PLUNGER_BLOCK =
        REGISTRATE.block("plunger", PlungerBlock::new)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .properties(properties -> properties.strength(0.5f))
                .item()
                .properties(properties -> properties.stacksTo(16))
                .build().register();

    public static final BlockEntry<PlungerHarpoonBlock> PLUNGER_HARPOON_BLOCK =
        REGISTRATE.block("plunger_harpoon", PlungerHarpoonBlock::new)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .properties(properties -> properties.strength(1.5f))
                .item()
                .properties(properties -> properties.stacksTo(1))
                .build().register();


    public static final BlockEntry<PlungerHarpoonAnchorBlock> PLUNGER_HARPOON_ANCHOR =
        REGISTRATE.block("plunger_harpoon_anchor", PlungerHarpoonAnchorBlock::new)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .properties(properties -> properties.strength(0.5f))
                .register();

    public static final BlockEntry<PlungerWheelBlock> PLUNGER_WHEEL =
            REGISTRATE.block("plunger_wheel", PlungerWheelBlock::new)
                    .properties(properties -> properties.strength(1.0f))
                    .item()
                    .properties(properties -> properties.component(OffroadDataComponents.TIRE,
                    new TireLike(0.75F, new Vec3(90.0D, 0.0D, 0.0D), Vec3.ZERO,
                            ResourceLocation.fromNamespaceAndPath(CreateBadPiggies.MODID, "block/plunger_wheel"), 0.0F)))
                    .build().register();

    public static final BlockEntry<UmbrellaBlock> UMBRELLA_BLOCK =
            REGISTRATE.block("umbrella", UmbrellaBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(properties -> properties.strength(0.5f).sound(SoundType.WOOL))
                    .item()
                    .properties(properties -> properties.stacksTo(16))
                    .build().register();

    public static void load() {
        // Only exists to ensure the class is loaded
    }
}
