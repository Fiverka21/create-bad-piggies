package com.create.badpiggies.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class UmbrellaBlock extends DirectionalBlock implements BlockSubLevelLiftProvider {

    public static final MapCodec<UmbrellaBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, UmbrellaBlock::new)
    );

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public UmbrellaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(OPEN, true));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    // --- ATTACHMENT & PLACEMENT LOGIC ---

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(facing.getOpposite());
        BlockState attachedState = level.getBlockState(attachedPos);

        if (attachedState.getBlock() instanceof UmbrellaBlock) {
            return false;
        }

        return attachedState.isFaceSturdy(level, attachedPos, facing, SupportType.FULL);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos targetBasePos = clickedPos.relative(clickedFace.getOpposite());
        BlockState targetState = level.getBlockState(targetBasePos);

        if (targetState.getBlock() instanceof UmbrellaBlock) {
            return null;
        }

        if (!targetState.isFaceSturdy(level, targetBasePos, clickedFace, SupportType.FULL)) {
            return null;
        }

        boolean hasSignal = level.hasNeighborSignal(clickedPos);

        BlockState state = this.defaultBlockState()
                .setValue(FACING, clickedFace)
                .setValue(OPEN, !hasSignal);

        if (state.canSurvive(level, clickedPos)) {
            return state;
        }
        return null;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            boolean shouldBeOpen = !hasSignal;

            if (state.getValue(OPEN) != shouldBeOpen) {
                level.setBlock(pos, state.setValue(OPEN, shouldBeOpen), 3);
            }
        }
    }

    // --- SABLE / SIMULATED PHYSICS INTERFACE ---

    @Override
    public @NotNull Direction sable$getNormal(final BlockState blockState) {
        return blockState.getValue(FACING);
    }

    @Override
    public float sable$getParallelDragScalar() {
        return 0.0f; // Handled dynamically below to prevent Sable's default normal projection
    }

    @Override
    public float sable$getDirectionlessDragScalar() {
        return 0.0f;
    }

    @Override
    public float sable$getLiftScalar() {
        return 0.0f;
    }

    @Override
    public void sable$contributeLiftAndDrag(final LiftProviderContext ctx, final ServerSubLevel subLevel,
                                            @NotNull final Pose3d localPose, final double timeStep,
                                            final Vector3dc linearVelocity, final Vector3dc angularVelocity,
                                            final Vector3d linearImpulse, final Vector3d angularImpulse,
                                            @Nullable final LiftProviderGroup group) {

        // If powered by redstone (closed), skip drag completely
        if (ctx == null || ctx.state() == null || !ctx.state().getValue(OPEN)) {
            return;
        }

        BlockSubLevelLiftProvider.resetVectors();
        LIFT_NORMAL.set(ctx.dir().x(), ctx.dir().y(), ctx.dir().z());
        LIFT_POS.set(ctx.pos().getX() + 0.5, ctx.pos().getY() + 0.5, ctx.pos().getZ() + 0.5);

        if (localPose != null) {
            localPose.transformNormal(LIFT_NORMAL);
            localPose.transformPosition(LIFT_POS);
        }

        final Pose3d pose = subLevel.logicalPose();
        final double pressure = DimensionPhysicsData.getAirPressure(subLevel.getLevel(), pose.transformPosition(LIFT_POS, TEMP));

        // Get local velocity at the block position
        pose.transformPosition(LIFT_POS, TEMP).sub(pose.position());
        LIFT_VELO.set(linearVelocity).add(angularVelocity.cross(TEMP, TEMP));
        pose.transformNormalInverse(LIFT_VELO);

        double velLength = LIFT_VELO.length();
        if (velLength < 1e-4) {
            BlockSubLevelLiftProvider.resetVectors();
            return;
        }

        // 1. Calculate how directly the umbrella canopy faces into incoming air
        // Air vector is opposite to movement velocity
        Vector3d airDir = new Vector3d(LIFT_VELO).negate().normalize();
        double exposure = Math.max(0.0, LIFT_NORMAL.dot(airDir)); // Only catch air if dish faces towards airflow

        if (exposure > 0) {
            // 2. Drag pushes OPPOSITE to movement (into air stream), scaled by exposure & speed
            double dragCoeff = 10.0f;
            double dragStrength = velLength * exposure * dragCoeff * pressure * timeStep;

            Vector3d dragForce = airDir.mul(dragStrength, DRAG);

            // Apply linear force
            linearImpulse.add(dragForce);

            // Apply off-center rotational torque relative to mass center!
            LIFT_POS.sub(subLevel.getMassTracker().getCenterOfMass(), TEMP);
            angularImpulse.add(TEMP.cross(dragForce));

            if (group != null) {
                group.totalDrag().add(dragForce);
                group.dragCenter().fma(dragStrength, LIFT_POS);
                group.totalDragStrength += dragStrength;
            }
        }

        BlockSubLevelLiftProvider.resetVectors();
    }
}