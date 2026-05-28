package com.kollekki.tormentum.Items;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class Mop extends Item {

    public Mop(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        player.startUsingItem(hand);

        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public void onUseTick(
            Level level,
            LivingEntity living,
            ItemStack stack,
            int remainingUseDuration
    ) {
        if (level.isClientSide()) {
            return;
        }

        if (!(living instanceof Player player)) {
            return;
        }

        if (remainingUseDuration % 4 != 0) {
            return;
        }

        HitResult result = player.pick(3.0D, 0.0F, false);

        if (!(result instanceof BlockHitResult hit)) {
            return;
        }

        BlockPos center = hit.getBlockPos();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-1, 0, -1),
                center.offset(1, 0, 1)
        )) {

            BlockState state = level.getBlockState(pos);

            boolean cleaned =
                    state.is(Tormentum.BLOOD_STAIN.get()) ||
                            state.is(Tormentum.BLOOD_PUDDLE.get()) ||
                                state.is(Tormentum.CHALK_BLOCK.get());

            if (!cleaned) {
                continue;
            }

            level.removeBlock(pos, false);

            if (level instanceof ServerLevel serverLevel) {

                RandomSource random = serverLevel.getRandom();

                serverLevel.sendParticles(
                        Tormentum.WATER_PUDDLE_PARTICLE.get(),
                        pos.getX() + 0.5,
                        pos.getY() + 0.02,
                        pos.getZ() + 0.5,
                        1,
                        0,
                        0,
                        0,
                        0
                );

                serverLevel.sendParticles(
                        ParticleTypes.SPLASH,
                        pos.getX() + 0.5,
                        pos.getY() + 0.05,
                        pos.getZ() + 0.5,
                        3,
                        0.15,
                        0.0,
                        0.15,
                        0.01
                );

                serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.MUD_HIT,
                        SoundSource.BLOCKS,
                        0.35f,
                        0.9f + random.nextFloat() * 0.2f
                );
            }
        }
    }
}