package com.kollekki.tormentum.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class ChalkBlock extends Block {
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;

	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP =
		Maps.newEnumMap(ImmutableMap.of(
			Direction.NORTH, NORTH,
			Direction.EAST, EAST,
			Direction.SOUTH, SOUTH,
			Direction.WEST, WEST
		));

	private static final VoxelShape BASE_SHAPE = Block.box(3, 0, 3, 13, 1, 13);

	private static final Map<Direction, VoxelShape> SIDE_SHAPES =
		Maps.newEnumMap(ImmutableMap.of(
			Direction.NORTH, Block.box(3, 0, 0, 13, 1, 13),
			Direction.SOUTH, Block.box(3, 0, 3, 13, 1, 16),
			Direction.EAST, Block.box(3, 0, 3, 16, 1, 13),
			Direction.WEST, Block.box(0, 0, 3, 13, 1, 13)
		));

	private final Map<BlockState, VoxelShape> shapes = Maps.newHashMap();

	public ChalkBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(NORTH, RedstoneSide.NONE)
			.setValue(EAST, RedstoneSide.NONE)
			.setValue(SOUTH, RedstoneSide.NONE)
			.setValue(WEST, RedstoneSide.NONE)
		);

		for (BlockState state : this.getStateDefinition().getPossibleStates()) {
			shapes.put(state, makeShape(state));
		}
	}

	private VoxelShape makeShape(BlockState state) {
		VoxelShape shape = BASE_SHAPE;

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (state.getValue(FACING_PROPERTY_MAP.get(dir)).isConnected()) {
				shape = Shapes.or(shape, SIDE_SHAPES.get(dir));
			}
		}

		return shape;
	}

	private RedstoneSide getSide(BlockGetter level, BlockPos pos, Direction dir) {
		BlockPos offset = pos.relative(dir);
		BlockState state = level.getBlockState(offset);

		if (state.is(this)) return RedstoneSide.SIDE;

		if (!state.isFaceSturdy(level, offset, dir.getOpposite())) {
			BlockState below = level.getBlockState(offset.below());
			if (below.is(this)) return RedstoneSide.SIDE;
		}

		return RedstoneSide.NONE;
	}

	private BlockState updateState(BlockGetter level, BlockPos pos) {
		BlockState state = this.defaultBlockState();

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			state = state.setValue(FACING_PROPERTY_MAP.get(dir), getSide(level, pos, dir));
		}

		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		BlockState existing = ctx.getLevel().getBlockState(pos);

		if (existing.is(this)) return null;

		return updateState(ctx.getLevel(), pos);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	public BlockState updateShape(
		BlockState state,
		net.minecraft.world.level.LevelReader level,
		net.minecraft.world.level.ScheduledTickAccess tickAccess,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		net.minecraft.util.RandomSource random
	) {
		if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
			return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
		}

		return updateState(level, pos);
	}
	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!state.canSurvive(level, pos)) {
			level.removeBlock(pos, false);
			return;
		}

		BlockState newState = updateState(level, pos);
		if (newState != state) {
			level.setBlock(pos, newState, 2);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return shapes.get(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST);
	}
}