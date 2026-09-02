package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** A redstone-triggered, short-duration rocket for Aeronautics vehicles. */
public class SodaBottleBlock extends DirectionalBlock implements BlockSubLevelLiftProvider {
    public static final MapCodec<SodaBottleBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, SodaBottleBlock::new));
    public static final DirectionProperty DIRECTION = DirectionProperty.create("direction");
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");
    private static final int ACTIVE_TICKS = 7 * 20;
    private static final VoxelShape BOTTLE_SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public SodaBottleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(DIRECTION, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(CHARGED, true));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DIRECTION, ACTIVE, CHARGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction attachment = context.getClickedFace();
        Direction direction = perpendicularDirection(attachment, context.getHorizontalDirection());
        BlockState state = defaultBlockState().setValue(FACING, attachment).setValue(DIRECTION, direction);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    private static Direction perpendicularDirection(Direction attachment, Direction preferred) {
        if (preferred.getAxis() != attachment.getAxis()) {
            return preferred;
        }
        return attachment.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos attachedPos = pos.relative(state.getValue(FACING).getOpposite());
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos,
                state.getValue(FACING), SupportType.FULL);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   BlockPos fromPos, boolean isMoving) {
        tryActivate(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        tryActivate(state, level, pos);
    }

    private void tryActivate(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide && state.getValue(CHARGED) && !state.getValue(ACTIVE)
                && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(ACTIVE, true).setValue(CHARGED, false), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, ACTIVE_TICKS);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }
        level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        return BOTTLE_SHAPE;
    }

    @Override
    public @NotNull Direction sable$getNormal(BlockState state) {
        return state.getValue(DIRECTION).getOpposite();
    }

    @Override
    public float sable$getParallelDragScalar() {
        return 0.0F;
    }

    @Override
    public float sable$getDirectionlessDragScalar() {
        return 0.0F;
    }

    @Override
    public float sable$getLiftScalar() {
        return 0.0F;
    }

    @Override
    public void sable$contributeLiftAndDrag(BlockSubLevelLiftProvider.LiftProviderContext ctx,
                                            ServerSubLevel subLevel, @NotNull Pose3d localPose,
                                            double timeStep, Vector3dc linearVelocity, Vector3dc angularVelocity,
                                            Vector3d linearImpulse, Vector3d angularImpulse,
                                            @Nullable BlockSubLevelLiftProvider.LiftProviderGroup group) {
        if (!ctx.state().getValue(ACTIVE)) {
            return;
        }

        BlockSubLevelLiftProvider.resetVectors();
        BlockSubLevelLiftProvider.LIFT_FORCE.set(ctx.dir().x(), ctx.dir().y(), ctx.dir().z())
                .mul(360.0D * timeStep);
        BlockSubLevelLiftProvider.LIFT_POS.set(ctx.pos().getX() + 0.5D,
                ctx.pos().getY() + 0.5D, ctx.pos().getZ() + 0.5D);
        if (localPose != null) {
            localPose.transformNormal(BlockSubLevelLiftProvider.LIFT_FORCE);
            localPose.transformPosition(BlockSubLevelLiftProvider.LIFT_POS);
        }

        linearImpulse.add(BlockSubLevelLiftProvider.LIFT_FORCE);
        BlockSubLevelLiftProvider.LIFT_POS.sub(subLevel.getMassTracker().getCenterOfMass(),
                BlockSubLevelLiftProvider.TEMP);
        angularImpulse.add(BlockSubLevelLiftProvider.TEMP.cross(BlockSubLevelLiftProvider.LIFT_FORCE));
        BlockSubLevelLiftProvider.resetVectors();
    }
}
