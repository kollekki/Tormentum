package com.kollekki.tormentum.effects;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.level.block.state.BlockState;

import static com.kollekki.tormentum.Tormentum.bleedingDamage;

public class BleedingEffect extends MobEffect {

    public BleedingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        BlockPos feetPos = entity.blockPosition();
        BlockPos belowPos = feetPos.below();

        BlockState feetState = level.getBlockState(feetPos);
        BlockState belowState = level.getBlockState(belowPos);

        if (feetState.canBeReplaced() &&
                belowState.isFaceSturdy(level, belowPos, net.minecraft.core.Direction.UP) &&
                level.getFluidState(feetPos).isEmpty()) {

            level.setBlock(feetPos, Tormentum.BLOOD_STAIN.get().defaultBlockState(), 3);

            entity.hurt(bleedingDamage(entity), 2.0F);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
    }
}