package de.selsus74.metalworkingbench;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class BenchResultSlot extends SlotItemHandler {
    private final MetalworkingBenchBlockEntity bench;

    public BenchResultSlot(MetalworkingBenchBlockEntity bench, int x, int y) {
        super(bench.getItems(), MetalworkingBenchBlockEntity.OUTPUT, x, y);
        this.bench = bench;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false; // take only
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        bench.onResultTaken(player);
        super.onTake(player, stack);
    }
}
