package de.selsus74.metalworkingbench;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MetalworkingBench.MOD_ID);

    public static final RegistryObject<BlockEntityType<MetalworkingBenchBlockEntity>> METALWORKING_BENCH =
            BLOCK_ENTITIES.register("metalworking_bench", () -> BlockEntityType.Builder
                    .of(MetalworkingBenchBlockEntity::new, ModBlocks.METALWORKING_BENCH.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}