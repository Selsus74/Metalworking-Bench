package de.selsus74.metalworkingbench;

import net.minecraft.util.StringRepresentable;

/** The two halves of the bench. MAIN holds the block entity, EXTENSION is just the second block. */
public enum BenchPart implements StringRepresentable {
    MAIN("main"),
    EXTENSION("extension");

    private final String name;

    BenchPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}