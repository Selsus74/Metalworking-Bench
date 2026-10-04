package de.selsus74.metalworkingbench;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MetalworkingBench.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MetalworkingBench.MOD_ID);

    public static final RegistryObject<RecipeSerializer<BenchRecipe>> BENCH_SERIALIZER =
            SERIALIZERS.register("metalworking", BenchRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<BenchRecipe>> BENCH_TYPE =
            TYPES.register("metalworking",
                    () -> RecipeType.<BenchRecipe>simple(new ResourceLocation(MetalworkingBench.MOD_ID, "metalworking")));

    public static boolean isTool(Level level, ItemStack stack) {
        return level.getRecipeManager().getAllRecipesFor(BENCH_TYPE.get()).stream()
                .anyMatch(recipe -> recipe.tool.test(stack));
    }

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
