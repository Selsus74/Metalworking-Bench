package de.selsus74.metalworkingbench;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ModelShapes {
    private static final Gson GSON = new Gson();

    private ModelShapes() {
    }

    //---get model elements from model JSONs and parse the dims into a voxel shape---
    //---makes block models automatically have pixel identical hitboxes---
    public static VoxelShape fromModel(ResourceLocation modelId) {
        String path = "/assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json";
        try (InputStream in = ModelShapes.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Model not found: " + path);

            //---make JSON root object---
            JsonObject root = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            //---get all elements as JSON array---
            JsonArray elements = root.getAsJsonArray("elements");

            //---create empty voxel---
            VoxelShape shape = Shapes.empty();
            //---for each shape element in elements JSON array add exact dimensions as shape in voxel---
            for (JsonElement el : elements) {
                JsonObject o = el.getAsJsonObject();
                JsonArray f = o.getAsJsonArray("from");
                JsonArray t = o.getAsJsonArray("to");
                shape = Shapes.or(shape, Block.box(
                        f.get(0).getAsDouble(), f.get(1).getAsDouble(), f.get(2).getAsDouble(),
                        t.get(0).getAsDouble(), t.get(1).getAsDouble(), t.get(2).getAsDouble()));
            }
            return shape.optimize();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read model: " + path, e);
        }
    }
}