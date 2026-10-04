package de.selsus74.metalworkingbench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Two blocks wide. The extension block sits to the RIGHT of the main block, seen from the front. The anvil surface is on the main block.
 * Only the main block has a block entity; clicking either half opens the same GUI.
 */
public class MetalworkingBenchBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BenchPart> PART = EnumProperty.create("part", BenchPart.class);

    // Hitbox / collision per facing. Shapes are written for facing = NORTH and rotated afterwards.
    private static final Map<Direction, VoxelShape> MAIN_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> EXTENSION_SHAPES = new EnumMap<>(Direction.class);

    static {
        VoxelShape tableTop = Block.box(0, 11, 0, 16, 13, 16);

        VoxelShape main = Shapes.or(tableTop,
                Block.box(12, 0, 1, 15, 11, 4),     // leg
                Block.box(12, 0, 12, 15, 11, 15),   // leg
                Block.box(1, 11, 4, 15, 14, 12),    // anvil top
                Block.box(0, 13, 15, 16, 23, 16));  // backwall

        VoxelShape extension = Shapes.or(tableTop,
                Block.box(0, 0, 0, 11, 11, 16),     // leg
                Block.box(0, 13, 15, 16, 23, 16));  // backwall

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int turns = (direction.get2DDataValue() + 2) % 4; // NORTH = 0 turns, EAST = 1, ...
            MAIN_SHAPES.put(direction, rotateShape(main, turns));
            EXTENSION_SHAPES.put(direction, rotateShape(extension, turns));
        }
    }

    /** Rotates a shape clockwise (seen from above) around the block center by 90 degrees per turn. */
    private static VoxelShape rotateShape(VoxelShape shape, int turns) {
        VoxelShape result = shape;
        for (int i = 0; i < turns; i++) {
            VoxelShape[] rotated = {Shapes.empty()};
            result.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                    rotated[0] = Shapes.or(rotated[0], Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2)));
            result = rotated[0];
        }
        return result;
    }

    public MetalworkingBenchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BenchPart.MAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    // ---------- helpers ----------

    /** Direction from this half to the other half. */
    private static Direction towardsOtherHalf(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(PART) == BenchPart.MAIN ? facing.getCounterClockWise() : facing.getClockWise();
    }

    private static BlockPos mainPos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == BenchPart.MAIN ? pos : pos.relative(towardsOtherHalf(state));
    }

    // ---------- shape / rendering ----------

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Map<Direction, VoxelShape> shapes = state.getValue(PART) == BenchPart.MAIN ? MAIN_SHAPES : EXTENSION_SHAPES;
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // BaseEntityBlock defaults to invisible
    }

    // ---------- placement ----------

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite(); // front faces the player
        BlockPos otherPos = context.getClickedPos().relative(facing.getCounterClockWise());
        if (!context.getLevel().getBlockState(otherPos).canBeReplaced(context)) {
            return null; // no space for the second half -> can't place
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, BenchPart.MAIN);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockPos otherPos = pos.relative(towardsOtherHalf(state));
        level.setBlock(otherPos, state.setValue(PART, BenchPart.EXTENSION), Block.UPDATE_ALL);
    }

    // ---------- keeping both halves together ----------

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == towardsOtherHalf(state)) {
            boolean partnerOk = neighborState.is(this)
                    && neighborState.getValue(PART) != state.getValue(PART)
                    && neighborState.getValue(FACING) == state.getValue(FACING);
            if (!partnerOk) {
                return Blocks.AIR.defaultBlockState(); // other half is gone -> this one goes too
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // In creative mode remove the other half without dropping the item twice.
        if (!level.isClientSide() && player.isCreative()) {
            BlockPos otherPos = pos.relative(towardsOtherHalf(state));
            BlockState other = level.getBlockState(otherPos);
            if (other.is(this) && other.getValue(PART) != state.getValue(PART)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    // ---------- block entity / GUI ----------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == BenchPart.MAIN ? new MetalworkingBenchBlockEntity(pos, state) : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockPos mainPos = mainPos(state, pos);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(mainPos) instanceof MetalworkingBenchBlockEntity bench) {
            NetworkHooks.openScreen(serverPlayer, bench, mainPos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MetalworkingBenchBlockEntity bench) {
            bench.dropContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
