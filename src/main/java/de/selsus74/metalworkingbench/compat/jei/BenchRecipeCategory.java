package de.selsus74.metalworkingbench.compat.jei;

import de.selsus74.metalworkingbench.BenchRecipe;
import de.selsus74.metalworkingbench.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import mezz.jei.api.recipe.RecipeType;

public class BenchRecipeCategory implements IRecipeCategory<BenchRecipe> {
    public static final RecipeType<BenchRecipe> TYPE =
            RecipeType.create("metalworkingbench", "bench", BenchRecipe.class);

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("metalworkingbench", "textures/gui/metalworking_bench_jei.png");

    private final IDrawable background;
    private final IDrawable icon;

    public BenchRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 120, 40)
                .setTextureSize(120, 40)
                .build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.METALWORKING_BENCH.get()));
    }

    @Override public RecipeType<BenchRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.metalworkingbench.metalworking_bench"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BenchRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 12).addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.CATALYST, 43, 12).addIngredients(recipe.getTool());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 12).addItemStack(recipe.getResultItem(null));
    }
}