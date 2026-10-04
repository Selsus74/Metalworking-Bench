package de.selsus74.metalworkingbench;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MetalworkingBench.MOD_ID);

    public static final List<String> METALS = List.of("iron", "gold", "copper");

    public static final Map<String, RegistryObject<Item>> PLATES = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> RODS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> WIRES = new LinkedHashMap<>();

    static {
        for (String metal : METALS) {
            PLATES.put(metal, ITEMS.register(metal + "_plate", () -> new Item(new Item.Properties())));
            RODS.put(metal, ITEMS.register(metal + "_rod", () -> new Item(new Item.Properties())));
            WIRES.put(metal, ITEMS.register(metal + "_wire", () -> new Item(new Item.Properties())));
        }
    }

    public static final RegistryObject<Item> HAMMER = ITEMS.register("hammer",
            () -> new Item(new Item.Properties().durability(250)));
    public static final RegistryObject<Item> WIRECUTTER = ITEMS.register("wirecutter",
            () -> new Item(new Item.Properties().durability(250)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}