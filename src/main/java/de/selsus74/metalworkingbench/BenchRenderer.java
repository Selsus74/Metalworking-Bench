package de.selsus74.metalworkingbench;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BenchRenderer implements BlockEntityRenderer<MetalworkingBenchBlockEntity> {

    //---Values are in blocks (1 block = 16 pixels) - pixels/16=blocks---
    private static final double INPUT_ITEM_X = 0.35;        // centre of the block
    private static final double INPUT_ITEM_Y = 1.08;        // height of the working surface + a tiny offset
    private static final double INPUT_ITEM_Z = 0.45;        // centre of the block
    private static final float INPUT_ITEM_SCALE = 0.5f;     // item size on the surface

    //---Tools from the extra storage: x, y, z, rot, rot---
    private static final double[][] TOOL_SLOTS = {
            {-0.10, 0.83, 0.20, 80, 90},    // first tool (takes tool slot if possible) - displayed: front
            {0.60, 1.15, 0.85, 0, 0},       // second - displayed: back(toolrack)
            {0.00, 1.15, 0.85, 0, 0},      // third - displayed: back(toolrack)
            {-0.65, 1.15, 0.85, 0, 0},      // forth - displayed: back(toolrack)
    };
    private static final float TOOL_ITEM_SCALE = 0.4f;      // item size on the surface

    public BenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MetalworkingBenchBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) {
            return;
        }

        //---packedLight is the light inside the block itself, which can be dark---
        int light = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().above());
        int seed = (int) be.getBlockPos().asLong();

        //---1) input item on the anvil---
        ItemStack input = be.getInputStack();
        if (!input.isEmpty()) {
            renderFunc(input, be, poseStack, buffer, light, INPUT_ITEM_X, INPUT_ITEM_Y, INPUT_ITEM_Z, 0f, 90, INPUT_ITEM_SCALE, seed);
        }

        //---2) tools from the extra storage---
        List<ItemStack> tools = be.getDisplayTools();
        int count = Math.min(tools.size(), TOOL_SLOTS.length);
        for (int i = 0; i < count; i++) {
            double[] slot = TOOL_SLOTS[i];
            renderFunc(tools.get(i), be, poseStack, buffer, light,
                    slot[0], slot[1], slot[2], (float) slot[3], (float) slot[4], TOOL_ITEM_SCALE, seed + i + 1);
        }
    }

    private void renderFunc(ItemStack stack, MetalworkingBenchBlockEntity be, PoseStack poseStack,
                            MultiBufferSource buffer, int light,
                            double x, double y, double z, float ypyaw, float xpyaw, float scale, int seed) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(MetalworkingBenchBlock.FACING);

        poseStack.pushPose();

        //---1) rotate around the block center (default orientation = NORTH)---
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - facing.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);

        //---2) from here on, write all positions as if the block faces north---
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(ypyaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(xpyaw));
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                be.getLevel(),
                seed
        );

        poseStack.popPose();
        poseStack.popPose();
    }
}