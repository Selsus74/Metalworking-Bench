package de.selsus74.metalworkingbench;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

import java.util.ArrayList;
import java.util.List;

public class MetalworkingBenchBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT = 0, TOOL = 1, OUTPUT = 2, SLOT_COUNT = 12;

    //---number of tools which can get rendered from the 3x3 storage---
    private static final int MAX_DISPLAY_TOOLS = 4;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == INPUT || slot == TOOL) {
                updateResult();
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public MetalworkingBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.METALWORKING_BENCH.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    //---the getter the renderer calls---
    public ItemStack getInputStack() {
        return items.getStackInSlot(INPUT);
    }

    //---getter for the tools (every item with durability) in the storage 3x3---
    public List<ItemStack> getDisplayTools() {
        List<ItemStack> result = new ArrayList<>(MAX_DISPLAY_TOOLS);
        for (int i = 0; i < items.getSlots() && result.size() < MAX_DISPLAY_TOOLS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                result.add(stack);
            }
        }
        return result;
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

    // send the inventory to the client when the chunk loads or the block updates
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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