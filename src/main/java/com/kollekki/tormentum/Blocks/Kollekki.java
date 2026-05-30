package com.kollekki.tormentum.Blocks;

import com.kollekki.tormentum.Tormentum;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Kollekki extends Block {
    public static final MapCodec<CrackedSkullBlock> CODEC = simpleCodec(CrackedSkullBlock::new);

    public static final IntegerProperty FACING = IntegerProperty.create("facing", 0, 7);

    private static final VoxelShape SHAPE = Shapes.box(
            0.25D, 0.0D, 0.25D,
            0.75D, 0.5D, 0.75D
    );

    public Kollekki(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int facing = Mth.floor((context.getPlayer().getYRot() * 8.0F / 360.0F) + 0.5D) & 7;
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide()) {
            level.playSound(
                    null,
                    pos,
                    Tormentum.KOLLEKKI_SOUND.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }

        return InteractionResult.SUCCESS;
    }
}