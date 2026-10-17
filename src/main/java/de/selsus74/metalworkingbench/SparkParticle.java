package de.selsus74.metalworkingbench;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

//---class start---
public class SparkParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    //---custom particle properties---
    protected SparkParticle(ClientLevel level, double x, double y, double z,
                            double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.gravity = 0.6F;          // falls like a real spark
        this.friction = 0.96F;        // slight air drag
        this.lifetime = 12 + this.random.nextInt(10);
        this.quadSize = 0.04F + this.random.nextFloat() * 0.03F;
        this.hasPhysics = true;       // bounces/stops on blocks (e.g. the table)
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);          // animates through frames over its lifetime
        this.alpha = 1.0F - (float) this.age / this.lifetime; // fade out
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880; // full brightness, sparks glow in the dark
    }

    //---nested class---
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SparkParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}