package de.selsus74.metalworkingbench;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class BenchRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    final Ingredient tool;
    final Ingredient input;
    final ItemStack result;

    public BenchRecipe(ResourceLocation id, Ingredient tool, Ingredient input, ItemStack result) {
        this.id = id;
        this.tool = tool;
        this.input = input;
        this.result = result;
    }

    // Container layout: slot 0 = input, slot 1 = tool
    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return input.test(container.getItem(0)) && tool.test(container.getItem(1));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BENCH_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BENCH_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BenchRecipe> {
        @Override
        public BenchRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient tool = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new BenchRecipe(id, tool, input, result);
        }

        @Override
        public BenchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient tool = Ingredient.fromNetwork(buf);
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            return new BenchRecipe(id, tool, input, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BenchRecipe recipe) {
            recipe.tool.toNetwork(buf);
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.result);
        }
    }
}
