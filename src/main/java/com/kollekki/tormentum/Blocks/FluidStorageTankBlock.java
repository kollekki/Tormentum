package com.kollekki.tormentum.Blocks;

import com.kollekki.tormentum.BlockEntities.FluidStorageTankBlockEntity;
import com.kollekki.tormentum.Tormentum;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FluidStorageTankBlock extends BaseEntityBlock {

    public static final MapCodec<FluidStorageTankBlock> CODEC =
            simpleCodec(FluidStorageTankBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public FluidStorageTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidStorageTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                Tormentum.FLUID_STORAGE_TANK_BE.get(),
                FluidStorageTankBlockEntity::tick
        );
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {

        if (!level.isClientSide()) {

            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof FluidStorageTankBlockEntity tank) {

                FluidStack fluid = tank.getStoredFluid();

                player.sendSystemMessage(Component.literal(
                        "Blood stored: "
                                + fluid.getAmount()
                                + " / "
                                + FluidStorageTankBlockEntity.CAPACITY
                                + " mB"
                ));
            }
        }

        return InteractionResult.SUCCESS;
    }
}