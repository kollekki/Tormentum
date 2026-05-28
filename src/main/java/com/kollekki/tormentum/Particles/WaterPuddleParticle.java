package com.kollekki.tormentum.Particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class WaterPuddleParticle extends SimpleAnimatedParticle {

    private final float targetSize;

    protected WaterPuddleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            SpriteSet sprites
    ) {
        super(level, x, y, z, sprites, 0f);

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.gravity = 0f;

        this.friction = 1f;

        this.hasPhysics = false;

        this.lifetime = 50 + this.random.nextInt(20);

        this.roll = this.random.nextFloat() * Mth.TWO_PI;

        this.oRoll = this.roll;

        this.targetSize = 0.58f + this.random.nextFloat() * 0.12f;

        this.quadSize = this.targetSize;

        this.setAlpha(0.72f);

        this.setColor(
                0.82f + this.random.nextFloat() * 0.08f,
                0.82f + this.random.nextFloat() * 0.08f,
                0.9f
        );
    }

    @Override
    public void tick() {
        super.tick();

        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        float life = (float)this.age / this.lifetime;

        this.quadSize = this.targetSize * (0.96f + (1f - life) * 0.04f);
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {

        return (rotation, camera, partialTick) -> {

            rotation.rotationX((float)-Math.PI / 2f);

            if (this.roll != 0.0F) {
                rotation.rotateZ(this.roll);
            }
        };
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                RandomSource random
        ) {
            return new WaterPuddleParticle(
                    level,
                    x,
                    y + 0.01 + random.nextDouble() * 0.003,
                    z,
                    sprites
            );
        }
    }
}