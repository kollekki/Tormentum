package com.kollekki.tormentum.Blocks;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BloodStain extends Block {
    public enum StainVariant implements StringRepresentable {
        DOT("dot"),
        LINE_X("line_x"),
        LINE_Z("line_z"),
        DIAG_NE_SW("diag_ne_sw"),
        DIAG_NW_SE("diag_nw_se");

        private final String name;

        StainVariant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<StainVariant> VARIANT = EnumProperty.create("variant", StainVariant.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public BloodStain(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, StainVariant.DOT));
    }

    public static BlockState stateForMotion(Vec3 motion) {
        double ax = Math.abs(motion.x);
        double az = Math.abs(motion.z);

        if (ax < 0.02D && az < 0.02D) {
            return Tormentum.BLOOD_STAIN.get().defaultBlockState().setValue(VARIANT, StainVariant.DOT);
        }

        if (ax >= az * 1.7D) {
            return Tormentum.BLOOD_STAIN.get().defaultBlockState().setValue(VARIANT, StainVariant.LINE_X);
        }

        if (az >= ax * 1.7D) {
            return Tormentum.BLOOD_STAIN.get().defaultBlockState().setValue(VARIANT, StainVariant.LINE_Z);
        }

        if (motion.x * motion.z >= 0.0D) {
            return Tormentum.BLOOD_STAIN.get().defaultBlockState().setValue(VARIANT, StainVariant.DIAG_NW_SE);
        }

        return Tormentum.BLOOD_STAIN.get().defaultBlockState().setValue(VARIANT, StainVariant.DIAG_NE_SW);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.getFluidState(pos).isEmpty()) {
            return null;
        }

        return this.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (!level.getFluidState(pos).isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}