package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlockEntities;
import com.create.badpiggies.block.entity.AdjustableTntBlockEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.simibubi.create.foundation.block.IBE;

import java.util.function.BiConsumer;

/** TNT whose blast radius is set by adding up to ten gunpowder items. */
public class AdjustableTntBlock extends TntBlock implements IBE<AdjustableTntBlockEntity> {
    public static final MapCodec<TntBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, AdjustableTntBlock::new));
    public static final IntegerProperty GUNPOWDER = IntegerProperty.create("gunpowder", 0, 10);

    public AdjustableTntBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(UNSTABLE, false).setValue(GUNPOWDER, 0));
    }

    @Override
    public Class<AdjustableTntBlockEntity> getBlockEntityClass() {
        return AdjustableTntBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AdjustableTntBlockEntity> getBlockEntityType() {
        return CBPBlockEntities.ADJUSTABLE_TNT_BLOCK_ENTITY.get();
    }

    @Override
    public MapCodec<TntBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE, GUNPOWDER);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this) && level.hasNeighborSignal(pos)) {
            explode(level, pos, null);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos, null);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        explode(level, pos, explosion.getIndirectSourceEntity());
    }

    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> dropConsumer) {
        explode(level, pos, explosion.getIndirectSourceEntity());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hit) {
        if (stack.is(Items.GUNPOWDER)) {
            if (state.getValue(GUNPOWDER) >= 10) {
                return ItemInteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(GUNPOWDER, state.getValue(GUNPOWDER) + 1), Block.UPDATE_ALL);
                if (!player.getAbilities().instabuild) {
                    stack.consume(1, player);
                }
                level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.7F, 1.2F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            if (!level.isClientSide) {
                if (stack.is(Items.FLINT_AND_STEEL) && !player.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                } else if (stack.is(Items.FIRE_CHARGE) && !player.getAbilities().instabuild) {
                    stack.consume(1, player);
                }
                explode(level, pos, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static void explode(Level level, BlockPos pos, LivingEntity owner) {
        if (level.isClientSide || !(level.getBlockState(pos).getBlock() instanceof AdjustableTntBlock)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        int gunpowder = state.getValue(GUNPOWDER);
        level.removeBlock(pos, false);
        float radius = 2.0F + gunpowder * 0.4F;
        level.explode(owner, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                radius, Level.ExplosionInteraction.TNT);
    }
}
