package com.kollekki.tormentum.Blocks;

import com.kollekki.tormentum.BlockEntities.CrackedSkullBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import static com.kollekki.tormentum.Tormentum.CRACKED_SKULL_BLOCK_ENTITY;

public class CrackedSkullBlock extends Block implements EntityBlock {
    public static final MapCodec<CrackedSkullBlock> CODEC = simpleCodec(CrackedSkullBlock::new);
    public static final IntegerProperty FACING = IntegerProperty.create("facing", 0, 7);
    private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE =
            net.minecraft.world.phys.shapes.Shapes.box(
                    0.25D, 0.0D, 0.25D,
                    0.75D, 0.5D, 0.75D
            );
    public CrackedSkullBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrackedSkullBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CrackedSkullBlockEntity skull) {
            ItemStack skullItem = skull.getHeldItem();

            if (skullItem.isEmpty() && !stack.isEmpty()) {
                if (!level.isClientSide()) {
                    skull.setHeldItem(stack.split(1));
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }

            if (!skullItem.isEmpty()) {
                if (!level.isClientSide()) {
                    if (!player.getInventory().add(skullItem.copy())) {
                        player.drop(skullItem.copy(), false);
                    }
                    skull.setHeldItem(ItemStack.EMPTY);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CrackedSkullBlockEntity skull) {
            if (!level.isClientSide()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), skull.getHeldItem());
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (level.isClientSide()) return null;

        if (type == CRACKED_SKULL_BLOCK_ENTITY.get()) {
            return (BlockEntityTicker<T>)
                    (BlockEntityTicker<CrackedSkullBlockEntity>) CrackedSkullBlockEntity::tick;
        }
        return null;
    }
}