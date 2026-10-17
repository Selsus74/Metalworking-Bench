package de.selsus74.metalworkingbench;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

//---class start---
public class ParticleRenderer {
    //---
    private static final double ANVIL_TOP_Y = 17.0 / 16.0; //divide by 16px to get as blocks not px value

    public static void spawnSparks(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + ANVIL_TOP_Y;
        double cz = pos.getZ() + 0.5;
        RandomSource rand = level.random;

        for (int i = 0; i < 20; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double horizontal = 0.08 + rand.nextDouble() * 0.15;
            double vx = Math.cos(angle) * horizontal;
            double vy = 0.15 + rand.nextDouble() * 0.2;
            double vz = Math.sin(angle) * horizontal;

            level.sendParticles(ModParticles.SPARK.get(), cx, cy, cz, 0, vx, vy, vz, 1.0);
        }
    }
}