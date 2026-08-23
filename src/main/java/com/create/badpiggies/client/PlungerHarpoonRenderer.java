package com.create.badpiggies.client;

import com.create.badpiggies.entity.PlungerHarpoonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Renders the fired plunger and a slightly sagging rope back to its launcher. */
public class PlungerHarpoonRenderer extends ThrownItemRenderer<PlungerHarpoonEntity> {
    private static final int ROPE_SEGMENTS = 24;

    public PlungerHarpoonRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }

    @Override
    public void render(PlungerHarpoonEntity harpoon, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int light) {
        super.render(harpoon, yaw, partialTick, poseStack, buffer, light);
        BlockPos launcher = harpoon.getLauncher().orElse(null);
        if (launcher == null)
            return;

        Vec3 start = harpoon.getPosition(partialTick);
        Vec3 end = Vec3.atCenterOf(launcher);
        Vec3 rope = end.subtract(start);
        VertexConsumer vertices = buffer.getBuffer(RenderType.lines());
        poseStack.pushPose();
        for (int i = 0; i < ROPE_SEGMENTS; i++) {
            float a = i / (float) ROPE_SEGMENTS;
            float b = (i + 1) / (float) ROPE_SEGMENTS;
            addRopeVertex(vertices, rope, a);
            addRopeVertex(vertices, rope, b);
        }
        poseStack.popPose();
    }

    private static void addRopeVertex(VertexConsumer vertices, Vec3 rope, float progress) {
        double sag = Math.sin(progress * Math.PI) * -.35;
        vertices.addVertex((float) (rope.x * progress), (float) (rope.y * progress + sag),
                (float) (rope.z * progress)).setColor(88, 57, 33, 255).setNormal(0, 1, 0);
    }
}
