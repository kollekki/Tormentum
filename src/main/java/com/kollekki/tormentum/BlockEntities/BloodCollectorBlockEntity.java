package com.kollekki.tormentum.BlockEntities;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class BloodCollectorBlockEntity extends BlockEntity {

    private static final int RANGE = 4;
    private static final int TICK_INTERVAL = 20;
    private static final int BLOOD_STAIN_AMOUNT = 5;
    private static final int BLOOD_PUDDLE_AMOUNT = 20;

    private int tickCounter = 0;
    private int bufferedBlood = 0;

    private static final DustParticleOptions BLOOD_DUST_TRAVEL =
            new DustParticleOptions(0xFFDD0000, 1.1f);

    private static final DustParticleOptions BLOOD_DUST_ABSORB =
            new DustParticleOptions(0xFFFF0000, 1.8f);

    public BloodCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(Tormentum.BLOOD_COLLECTOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BloodCollectorBlockEntity be) {
        if (level.isClientSide()) return;

        be.tickCounter++;
        if (be.tickCounter < TICK_INTERVAL) return;
        be.tickCounter = 0;

        be.collectBloodBlocks(level, pos);
        be.tryInsertBelow(level, pos);
    }

    private void collectBloodBlocks(Level level, BlockPos origin) {

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        List<BlockPos> toRemove = new ArrayList<>();

        for (BlockPos scanPos : BlockPos.betweenClosed(
                origin.offset(-RANGE, -1, -RANGE),
                origin.offset(RANGE, 1, RANGE))) {

            BlockState blockState = level.getBlockState(scanPos);

            boolean stain = blockState.is(Tormentum.BLOOD_STAIN.get());
            boolean puddle = blockState.is(Tormentum.BLOOD_PUDDLE.get());

            if (!stain && !puddle) {
                continue;
            }

            toRemove.add(scanPos.immutable());

            if (stain) {
                bufferedBlood += BLOOD_STAIN_AMOUNT;
            } else {
                bufferedBlood += BLOOD_PUDDLE_AMOUNT;
            }

            spawnTravelParticles(serverLevel, scanPos, origin);

            serverLevel.playSound(
                    null,
                    scanPos,
                    SoundEvents.SCULK_BLOCK_SPREAD,
                    SoundSource.BLOCKS,
                    0.35f,
                    0.65f + level.getRandom().nextFloat() * 0.2f
            );
        }

        for (BlockPos removePos : toRemove) {
            level.setBlock(removePos, Blocks.AIR.defaultBlockState(), 3);
        }

        if (!toRemove.isEmpty()) {

            serverLevel.sendParticles(
                    BLOOD_DUST_ABSORB,
                    origin.getX() + 0.5,
                    origin.getY() + 0.7,
                    origin.getZ() + 0.5,
                    20,
                    0.25,
                    0.15,
                    0.25,
                    0.04
            );

            serverLevel.playSound(
                    null,
                    origin,
                    SoundEvents.SCULK_CATALYST_BLOOM,
                    SoundSource.BLOCKS,
                    1.0f,
                    0.8f
            );

            setChanged();
        }
    }

    private static void spawnTravelParticles(ServerLevel serverLevel, BlockPos from, BlockPos to) {

        double fx = from.getX() + 0.5;
        double fy = from.getY() + 0.08;
        double fz = from.getZ() + 0.5;

        double tx = to.getX() + 0.5;
        double ty = to.getY() + 0.7;
        double tz = to.getZ() + 0.5;

        double dx = tx - fx;
        double dy = ty - fy;
        double dz = tz - fz;

        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist == 0) {
            return;
        }

        for (int i = 0; i < 10; i++) {

            double jx = dx / dist + (Math.random() - 0.5) * 0.08;
            double jy = dy / dist + (Math.random() - 0.5) * 0.08;
            double jz = dz / dist + (Math.random() - 0.5) * 0.08;

            serverLevel.sendParticles(
                    BLOOD_DUST_TRAVEL,
                    fx,
                    fy,
                    fz,
                    0,
                    jx,
                    jy,
                    jz,
                    0.22
            );
        }
    }

    private void tryInsertBelow(Level level, BlockPos pos) {
        if (bufferedBlood <= 0) return;

        BlockPos below = pos.below();
        BlockEntity be = level.getBlockEntity(below);

        if (be instanceof FluidStorageTankBlockEntity tank) {
            FluidStack toInsert = new FluidStack(Tormentum.BLOOD_SOURCE.get(), bufferedBlood);
            int filled = tank.fill(toInsert);
            bufferedBlood -= filled;
            if (filled > 0) {
                be.setChanged();
                setChanged();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BufferedBlood", bufferedBlood);
        output.putInt("TickCounter", tickCounter);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        bufferedBlood = input.getIntOr("BufferedBlood", 0);
        tickCounter = input.getIntOr("TickCounter", 0);
    }
}