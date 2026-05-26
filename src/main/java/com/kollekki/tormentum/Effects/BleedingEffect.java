package com.kollekki.tormentum.Effects;

import com.kollekki.tormentum.Tormentum;
import com.kollekki.tormentum.Blocks.BloodStain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.kollekki.tormentum.Tormentum.bleedingDamage;

public class BleedingEffect extends MobEffect {
    private static final Map<UUID, Vec3> LAST_POS = new HashMap<>();
    private static final Map<UUID, Integer> DAMAGE_TIMER = new HashMap<>();

    public BleedingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        UUID id = entity.getUUID();
        Vec3 currentPos = entity.position();
        Vec3 lastPos = LAST_POS.put(id, currentPos);

        int timer = DAMAGE_TIMER.getOrDefault(id, 0) + 1;
        DAMAGE_TIMER.put(id, timer);

        BlockPos feetPos = entity.blockPosition();
        if (timer >= Math.max(10, 25 - amplifier * 5)) {
            DAMAGE_TIMER.put(id, 0);

            BlockState current = level.getBlockState(feetPos);
            boolean isStain   = current.is(Tormentum.BLOOD_STAIN.get());
            boolean isPuddle  = current.is(Tormentum.BLOOD_PUDDLE.get());

            if (isStain || isPuddle || current.canBeReplaced()) {

                BlockState puddle = isPuddle
                        ? current
                        : Tormentum.BLOOD_PUDDLE.get().defaultBlockState();

                boolean anyFace = isPuddle;

                BlockPos belowPos = feetPos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if (MultifaceBlock.canAttachTo(level, Direction.DOWN, belowPos, belowState)) {
                    puddle = puddle.setValue(BlockStateProperties.DOWN, true);
                    anyFace = true;
                }

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos wallPos   = feetPos.relative(dir);
                    BlockState wallState = level.getBlockState(wallPos);
                    if (MultifaceBlock.canAttachTo(level, dir, wallPos, wallState)) {
                        puddle = puddle.setValue(MultifaceBlock.getFaceProperty(dir), true);
                        anyFace = true;
                    }
                }

                if (anyFace) {
                    level.setBlock(feetPos, puddle, 3);
                }
            }

            entity.hurt(bleedingDamage(entity), 2.0F + amplifier);
        }

        if (lastPos == null || !entity.onGround()) {
            return true;
        }

        BlockPos lastFeetPos = BlockPos.containing(lastPos);
        if (lastFeetPos.equals(feetPos)) {
            return true;
        }

        BlockState feetState = level.getBlockState(feetPos);
        BlockPos belowPos    = feetPos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if (feetState.canBeReplaced()
                && belowState.isFaceSturdy(level, belowPos, Direction.UP)
                && level.getFluidState(feetPos).isEmpty()) {
            Vec3 motion = currentPos.subtract(lastPos);
            level.setBlock(feetPos, BloodStain.stateForMotion(motion), 3);
        }

        return true;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        float initialDamage = 5.0F + amplifier * 2.0F;
        entity.hurt(bleedingDamage(entity), initialDamage);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }
}