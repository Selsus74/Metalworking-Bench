package de.selsus74.metalworkingbench;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class MetalworkingBenchBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT = 0, TOOL = 1, OUTPUT = 2, SLOT_COUNT = 12;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == INPUT || slot == TOOL) {
                updateResult();
            }
            setChanged();
        }
    };

    public MetalworkingBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.METALWORKING_BENCH.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.metalworkingbench.metalworking_bench");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MetalworkingBenchMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
    }

    private void updateResult() {
        if (level == null || level.isClientSide()) return;

        ItemStack result = ItemStack.EMPTY;
        ItemStack input = items.getStackInSlot(INPUT);
        ItemStack tool = items.getStackInSlot(TOOL);
        if (!input.isEmpty() && !tool.isEmpty()) {
            SimpleContainer container = new SimpleContainer(input, tool);
            result = level.getRecipeManager()
                    .getRecipeFor(ModRecipes.BENCH_TYPE.get(), container, level)
                    .map(recipe -> recipe.assemble(container, level.registryAccess()))
                    .orElse(ItemStack.EMPTY);
        }
        items.setStackInSlot(OUTPUT, result);
    }

    public void onResultTaken(Player player) {
        if (level == null || level.isClientSide()) return;

        items.extractItem(INPUT, 1, false);
        items.getStackInSlot(TOOL).hurtAndBreak(1, player,
                p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        updateResult();
        setChanged();
    }

    public void dropContents() {
        if (level == null) return;
        SimpleContainer container = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) {
            if (i == OUTPUT) continue;
            container.setItem(i, items.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, container);
    }
}