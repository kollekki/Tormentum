package com.kollekki.tormentum.Blocks;

import com.kollekki.tormentum.Tormentum;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class MusicBoxBlock extends Block {

    public static final MapCodec<MusicBoxBlock> CODEC = simpleCodec(MusicBoxBlock::new);

    private static final Map<BlockPos, SoundInstance> activeSounds = new HashMap<>();

    public MusicBoxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<MusicBoxBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Minecraft mc = Minecraft.getInstance();
        SoundInstance existing = activeSounds.get(pos);
        if (existing != null && mc.getSoundManager().isActive(existing)) return;

        SimpleSoundInstance sound = new SimpleSoundInstance(
                Tormentum.MUS_MUSIC_BOX.get().location(),
                SoundSource.RECORDS,
                1.0f,
                1.0f,
                SoundInstance.createUnseededRandom(),
                true,    // looping
                0,
                SoundInstance.Attenuation.LINEAR,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                false    // relative
        );
        mc.getSoundManager().play(sound);
        activeSounds.put(pos, sound);
    }
}