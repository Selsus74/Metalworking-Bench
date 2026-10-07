package de.selsus74.metalworkingbench.compat.jei;

import de.selsus74.metalworkingbench.ModBlocks;
import de.selsus74.metalworkingbench.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class MetalworkingJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("metalworkingbench", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new BenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(BenchRecipeCategory.TYPE,
                rm.getAllRecipesFor(ModRecipes.BENCH_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.METALWORKING_BENCH.get()), BenchRecipeCategory.TYPE);
    }
}