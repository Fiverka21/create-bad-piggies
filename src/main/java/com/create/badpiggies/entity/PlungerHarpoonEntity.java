package com.create.badpiggies.entity;

import java.util.Optional;

import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.block.entity.PlungerHarpoonBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** A plunger projectile which plants a solid anchor block and tethers it to its launcher. */
public class PlungerHarpoonEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Optional<BlockPos>> ATTACHED_BLOCK =
            SynchedEntityData.defineId(PlungerHarpoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> LAUNCHER_BLOCK =
            SynchedEntityData.defineId(PlungerHarpoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public PlungerHarpoonEntity(EntityType<PlungerHarpoonEntity> type, Level level) {
        super(type, level);
    }

    public PlungerHarpoonEntity(Level level) {
        this(CreateBadPiggies.PLUNGER_HARPOON_PROJECTILE.get(), level);
        setItem(CreateBadPiggies.PLUNGER.get().getDefaultInstance());
    }

    @Override
    protected Item getDefaultItem() {
        return CreateBadPiggies.PLUNGER.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACHED_BLOCK, Optional.empty());
        builder.define(LAUNCHER_BLOCK, Optional.empty());
    }

    public boolean isAttached() {
        return entityData.get(ATTACHED_BLOCK).isPresent();
    }

    public void setLauncher(BlockPos launcher) {
        entityData.set(LAUNCHER_BLOCK, Optional.of(launcher.immutable()));
    }

    public Optional<BlockPos> getLauncher() {
        return entityData.get(LAUNCHER_BLOCK);
    }

    @Override
    public void tick() {
        if (isAttached()) {
            BlockPos pos = entityData.get(ATTACHED_BLOCK).orElseThrow();
            if (!level().getBlockState(pos).is(CreateBadPiggies.PLUNGER_HARPOON_ANCHOR.get()))
                discard();
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.tick();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!level().isClientSide) {
            BlockPos anchor = hit.getBlockPos().relative(hit.getDirection());
            if (level().getBlockState(anchor).canBeReplaced()) {
                level().setBlock(anchor, CreateBadPiggies.PLUNGER_HARPOON_ANCHOR.get().defaultBlockState(), Block.UPDATE_ALL);
            } else {
                // A non-replaceable space cannot contain a newly-created solid plunger.
                anchor = hit.getBlockPos();
            }

            entityData.set(ATTACHED_BLOCK, Optional.of(anchor));
            BlockPos finalAnchor = anchor;
            getLauncher().ifPresent(launcherPos -> {
                BlockEntity blockEntity = level().getBlockEntity(launcherPos);
                if (blockEntity instanceof PlungerHarpoonBlockEntity launcher) {
                    launcher.attachAnchor(finalAnchor);
                }
            });
            Vec3 location = hit.getLocation().add(hit.getDirection().getStepX() * .01,
                    hit.getDirection().getStepY() * .01, hit.getDirection().getStepZ() * .01);
            setPos(location);
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        entityData.get(ATTACHED_BLOCK).ifPresent(pos -> tag.putLong("AttachedBlock", pos.asLong()));
        entityData.get(LAUNCHER_BLOCK).ifPresent(pos -> tag.putLong("LauncherBlock", pos.asLong()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AttachedBlock"))
            entityData.set(ATTACHED_BLOCK, Optional.of(BlockPos.of(tag.getLong("AttachedBlock"))));
        if (tag.contains("LauncherBlock"))
            entityData.set(LAUNCHER_BLOCK, Optional.of(BlockPos.of(tag.getLong("LauncherBlock"))));
    }
}
