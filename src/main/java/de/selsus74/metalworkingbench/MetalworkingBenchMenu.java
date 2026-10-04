package de.selsus74.metalworkingbench;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MetalworkingBenchMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    // client side: block position comes from the network buffer
    public MetalworkingBenchMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, (MetalworkingBenchBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public MetalworkingBenchMenu(int id, Inventory inventory, MetalworkingBenchBlockEntity bench) {
        super(ModMenuTypes.METALWORKING_BENCH.get(), id);
        this.access = ContainerLevelAccess.create(bench.getLevel(), bench.getBlockPos());
        IItemHandler handler = bench.getItems();

        addSlot(new SlotItemHandler(handler, MetalworkingBenchBlockEntity.INPUT, 8, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !ModRecipes.isTool(bench.getLevel(), stack);
            }
        });
        addSlot(new SlotItemHandler(handler, MetalworkingBenchBlockEntity.TOOL, 42, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ModRecipes.isTool(bench.getLevel(), stack);
            }
        });
        addSlot(new BenchResultSlot(bench, 90, 42));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(handler, 3 + col + row * 3, 117 + col * 18, 24 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 160));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int benchSlots = MetalworkingBenchBlockEntity.SLOT_COUNT;
        int total = benchSlots + 36;

        if (index < benchSlots) {
            if (!moveItemStackTo(stack, benchSlots, total, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 2, false) && !moveItemStackTo(stack, 3, benchSlots, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.METALWORKING_BENCH.get());
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof BenchResultSlot) && super.canTakeItemForPickAll(stack, slot);
    }
}