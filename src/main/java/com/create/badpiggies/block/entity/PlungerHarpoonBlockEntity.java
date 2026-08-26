package com.create.badpiggies.block.entity;

import com.create.badpiggies.CBPBlockEntities;
import com.create.badpiggies.CBPEntityTypes;
import com.create.badpiggies.CreateBadPiggies;
import com.create.badpiggies.entity.PlungerHarpoonEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Stores plunger ammunition and launches it when powered by both a shaft and redstone. */
public class PlungerHarpoonBlockEntity extends KineticBlockEntity implements RopeStrandHolderBlockEntity {
    private RopeStrandHolderBehavior ropeHolder;

    private static final int MAX_AMMO = 16;
    private int ammo;
    private boolean wasPowered;

    public PlungerHarpoonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean load() {
        if (ammo >= MAX_AMMO) {
            return false;
        }
        ammo++;
        setChanged();
        return true;
    }

    public boolean unload() {
        if (ammo == 0) {
            return false;
        }
        ammo--;
        setChanged();
        return true;
    }

    /** Called by the projectile after it has placed its solid, rope-compatible plunger anchor. */
    public void attachAnchor(BlockPos anchor) {
        RopeStrandHolderBehavior target = level == null ? null
                : dev.simulated_team.simulated.content.items.rope.RopeItem.RopeItem.getRopeHolder(level, anchor);
        if (ropeHolder != null && target != null && !ropeHolder.isAttached() && !target.isAttached()) {
            ropeHolder.createRope(target, false);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(this.ropeHolder = new RopeStrandHolderBehavior(this));
    }

    @Override
    public RopeStrandHolderBehavior getBehavior() {
        return ropeHolder;
    }

    @Override
    public Vec3 getAttachmentPoint(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(DirectionalKineticBlock.FACING);
        return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5D));
    }

    public void tryFire() {
        boolean powered = level != null && level.hasNeighborSignal(worldPosition);
        if (powered && !wasPowered && ammo > 0 && Math.abs(getSpeed()) >= 8.0F) {
            Direction facing = getBlockState().getValue(DirectionalKineticBlock.FACING);
            Vec3 direction = Vec3.atLowerCornerOf(facing.getNormal());
            Vec3 barrel = Vec3.atCenterOf(worldPosition).add(direction.scale(.75D));
            PlungerHarpoonEntity harpoon = CBPEntityTypes.PLUNGER_HARPOON_PROJECTILE.create(level);
            harpoon.setPos(barrel);
            harpoon.setDeltaMovement(direction.scale(2.5D));
            harpoon.setLauncher(worldPosition);
            level.addFreshEntity(harpoon);
            level.playSound(null, worldPosition, SoundEvents.CROSSBOW_SHOOT, SoundSource.BLOCKS, 0.8F, 0.9F);
            ammo--;
            setChanged();
        }
        wasPowered = powered;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && !level.isClientSide && !level.hasNeighborSignal(worldPosition)) {
            wasPowered = false;
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("Ammo", ammo);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        ammo = tag.getInt("Ammo");
    }

    @Override
    public AABB getRenderBoundingBox() {
        final ClientRopeStrand rope = this.ropeHolder.getClientStrand();
        if (rope != null && this.ropeHolder.ownsRope()) {
            final AABB bounds = rope.getBounds();

            if (bounds == null) {
                return super.getRenderBoundingBox();
            }

            return bounds.inflate(RopeConnectorBlockEntity.RENDER_BOUNDING_BOX_INFLATION);
        } else {
            return super.getRenderBoundingBox();
        }
    }
}
