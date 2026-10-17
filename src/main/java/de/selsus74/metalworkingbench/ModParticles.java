package de.selsus74.metalworkingbench;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

//---class start---
public class ModParticles {
    //---registers the SPARK particle type---
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "metalworkingbench");

    public static final RegistryObject<SimpleParticleType> SPARK =
            PARTICLES.register("spark", () -> new SimpleParticleType(false));
}
