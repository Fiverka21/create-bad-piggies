package com.create.badpiggies.block;

import com.create.badpiggies.CBPBlockEntities;
import com.create.badpiggies.block.entity.PropulsiveFireworkBlockEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelLiftProvider;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/** A redstone-triggered rocket whose thrust scales with its gunpowder charge. */
public class PropulsiveFireworkBlock extends DirectionalBlock
        implements BlockSubLevelLiftProvider, IBE<PropulsiveFireworkBlockEntity> {
    public static final MapCodec<PropulsiveFireworkBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(propertiesCodec()).apply(instance, PropulsiveFireworkBlock::new));
    public static final DirectionProperty DIRECTION = DirectionProperty.create("direction");
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    public static final IntegerProperty GUNPOWDER = IntegerProperty.create("gunpowder", 0, 10);
    private static final int MIN_ACTIVE_TICKS = 5 * 20;
    private static final int MAX_ACTIVE_TICKS = 20 * 20;
    private static final VoxelShape SHAPE = Block.box(5, 2, 5, 11, 14, 11);

    public PropulsiveFireworkBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP)
                .setValue(DIRECTION, Direction.NORTH).setValue(ACTIVE, false).setValue(GUNPOWDER, 0));
    }

    @Override protected MapCodec<? extends DirectionalBlock> codec() { return CODEC; }
    @Override public Class<PropulsiveFireworkBlockEntity> getBlockEntityClass() { return PropulsiveFireworkBlockEntity.class; }
    @Override public BlockEntityType<? extends PropulsiveFireworkBlockEntity> getBlockEntityType() {
        return CBPBlockEntities.PROPULSIVE_FIREWORK_BLOCK_ENTITY.get();
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DIRECTION, ACTIVE, GUNPOWDER);
    }

    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        Direction attachment = context.getClickedFace();
        Direction direction = context.getHorizontalDirection().getAxis() != attachment.getAxis()
                ? context.getHorizontalDirection() : (attachment.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP);
        BlockState state = defaultBlockState().setValue(FACING, attachment).setValue(DIRECTION, direction);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos attached = pos.relative(state.getValue(FACING).getOpposite());
        return level.getBlockState(attached).isFaceSturdy(level, attached, state.getValue(FACING), SupportType.FULL);
    }
    @Override public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                             LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && state.getValue(GUNPOWDER) > 0 && !state.getValue(ACTIVE) && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, this, activeTicks(state.getValue(GUNPOWDER)));
            level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    private static int activeTicks(int gunpowder) {
        return MIN_ACTIVE_TICKS + (gunpowder - 1) * (MAX_ACTIVE_TICKS - MIN_ACTIVE_TICKS) / 9;
    }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        neighborChanged(state, level, pos, this, pos, movedByPiston);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, false).setValue(GUNPOWDER, 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!state.getValue(ACTIVE)) return;
        Direction exhaust = state.getValue(DIRECTION).getOpposite();
        double x = pos.getX() + 0.5D + exhaust.getStepX() * 0.45D;
        double y = pos.getY() + 0.5D + exhaust.getStepY() * 0.45D;
        double z = pos.getZ() + 0.5D + exhaust.getStepZ() * 0.45D;
        level.addParticle(ParticleTypes.FIREWORK, x, y, z,
                exhaust.getStepX() * 0.05D + random.nextGaussian() * 0.05D,
                exhaust.getStepY() * 0.05D + random.nextGaussian() * 0.05D,
                exhaust.getStepZ() * 0.05D + random.nextGaussian() * 0.05D);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                         Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.GUNPOWDER)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (state.getValue(GUNPOWDER) >= 10) return ItemInteractionResult.FAIL;
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(GUNPOWDER, state.getValue(GUNPOWDER) + 1), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.7F, 1.2F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public @NotNull Direction sable$getNormal(BlockState state) { return state.getValue(DIRECTION); }
    @Override public float sable$getParallelDragScalar() { return 0; }
    @Override public float sable$getDirectionlessDragScalar() { return 0; }
    @Override public float sable$getLiftScalar() { return 0; }
    @Override public void sable$contributeLiftAndDrag(BlockSubLevelLiftProvider.LiftProviderContext ctx, ServerSubLevel subLevel,
                                                       @NotNull Pose3d localPose, double timeStep, Vector3dc linearVelocity,
                                                       Vector3dc angularVelocity, Vector3d linearImpulse, Vector3d angularImpulse,
                                                       @Nullable BlockSubLevelLiftProvider.LiftProviderGroup group) {
        if (!ctx.state().getValue(ACTIVE)) return;
        BlockSubLevelLiftProvider.resetVectors();
        double force = ctx.state().getValue(GUNPOWDER) * 48.0D;
        BlockSubLevelLiftProvider.LIFT_FORCE.set(ctx.dir().x(), ctx.dir().y(), ctx.dir().z()).mul(force * timeStep);
        BlockSubLevelLiftProvider.LIFT_POS.set(ctx.pos().getX() + .5, ctx.pos().getY() + .5, ctx.pos().getZ() + .5);
        if (localPose != null) { localPose.transformNormal(BlockSubLevelLiftProvider.LIFT_FORCE); localPose.transformPosition(BlockSubLevelLiftProvider.LIFT_POS); }
        linearImpulse.add(BlockSubLevelLiftProvider.LIFT_FORCE);
        BlockSubLevelLiftProvider.LIFT_POS.sub(subLevel.getMassTracker().getCenterOfMass(), BlockSubLevelLiftProvider.TEMP);
        angularImpulse.add(BlockSubLevelLiftProvider.TEMP.cross(BlockSubLevelLiftProvider.LIFT_FORCE));
        BlockSubLevelLiftProvider.resetVectors();
    }
}
