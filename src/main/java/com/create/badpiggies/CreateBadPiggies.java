package com.create.badpiggies;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.create.badpiggies.block.PlungerWheelBlock;
import com.create.badpiggies.block.PlungerBlock;
import com.create.badpiggies.block.PlungerHarpoonBlock;
import com.create.badpiggies.block.PlungerHarpoonAnchorBlock;
import com.create.badpiggies.block.entity.PlungerWheelBlockEntity;
import com.create.badpiggies.block.entity.PlungerHarpoonBlockEntity;
import com.create.badpiggies.block.entity.PlungerHarpoonAnchorBlockEntity;
import com.create.badpiggies.entity.PlungerHarpoonEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateBadPiggies.MODID)
public class CreateBadPiggies {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "createbadpiggies";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<PlungerBlock> PLUNGER_BLOCK = BLOCKS.registerBlock("plunger",
            PlungerBlock::new, BlockBehaviour.Properties.of().strength(0.5F).noOcclusion());
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PLUNGER = ITEMS.registerSimpleBlockItem(PLUNGER_BLOCK,
            new Item.Properties().stacksTo(16));
    public static final DeferredBlock<PlungerHarpoonBlock> PLUNGER_HARPOON_BLOCK = BLOCKS.registerBlock("plunger_harpoon",
            PlungerHarpoonBlock::new, BlockBehaviour.Properties.of().strength(1.5F).noOcclusion());
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PLUNGER_HARPOON = ITEMS.registerSimpleBlockItem(
            PLUNGER_HARPOON_BLOCK, new Item.Properties().stacksTo(1));
    public static final DeferredHolder<EntityType<?>, EntityType<PlungerHarpoonEntity>> PLUNGER_HARPOON_PROJECTILE =
            ENTITY_TYPES.register("plunger_harpoon", () -> EntityType.Builder
                    .<PlungerHarpoonEntity>of(PlungerHarpoonEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(8).updateInterval(1).build("plunger_harpoon"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlungerHarpoonBlockEntity>> PLUNGER_HARPOON_ENTITY =
            BLOCK_ENTITY_TYPES.register("plunger_harpoon", () -> BlockEntityType.Builder
                    .of(PlungerHarpoonBlockEntity::new, PLUNGER_HARPOON_BLOCK.get()).build(null));
    public static final DeferredBlock<PlungerHarpoonAnchorBlock> PLUNGER_HARPOON_ANCHOR = BLOCKS.registerBlock(
            "plunger_harpoon_anchor", PlungerHarpoonAnchorBlock::new,
            BlockBehaviour.Properties.of().strength(0.5F).noOcclusion());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlungerHarpoonAnchorBlockEntity>>
            PLUNGER_HARPOON_ANCHOR_ENTITY = BLOCK_ENTITY_TYPES.register("plunger_harpoon_anchor", () ->
                    BlockEntityType.Builder.of(PlungerHarpoonAnchorBlockEntity::new, PLUNGER_HARPOON_ANCHOR.get()).build(null));
    public static final DeferredBlock<PlungerWheelBlock> PLUNGER_WHEEL = BLOCKS.registerBlock("plunger_wheel",
            PlungerWheelBlock::new, BlockBehaviour.Properties.of().strength(1.0F));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> PLUNGER_WHEEL_ITEM = ITEMS.registerSimpleBlockItem(
            PLUNGER_WHEEL, new Item.Properties().component(OffroadDataComponents.TIRE,
                    new TireLike(0.75F, new Vec3(90.0D, 0.0D, 0.0D), Vec3.ZERO,
                            ResourceLocation.fromNamespaceAndPath(MODID, "block/plunger_wheel"), 0.0F)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlungerWheelBlockEntity>> PLUNGER_WHEEL_ENTITY =
            BLOCK_ENTITY_TYPES.register("plunger_wheel", () -> BlockEntityType.Builder
                    .of(PlungerWheelBlockEntity::new, PLUNGER_WHEEL.get()).build(null));

    // Creates a creative tab with the id "createbadpiggies:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createbadpiggies")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> PLUNGER_WHEEL_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(PLUNGER.get());
                output.accept(PLUNGER_HARPOON.get());
                output.accept(PLUNGER_WHEEL_ITEM.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateBadPiggies(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CreateBadPiggies) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PLUNGER.get());
            event.accept(PLUNGER_HARPOON.get());
            event.accept(PLUNGER_WHEEL_ITEM.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
