package com.create.badpiggies.client;

import com.create.badpiggies.block.entity.PlungerHarpoonBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.RopeStrandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

/** Renders the Simulated rope owned by a plunger harpoon launcher. */
public class PlungerHarpoonRopeRenderer extends SafeBlockEntityRenderer<PlungerHarpoonBlockEntity> {
    public PlungerHarpoonRopeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(PlungerHarpoonBlockEntity launcher, float partialTick, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RopeStrandRenderer.render(launcher, launcher.getBehavior(), partialTick, poseStack, buffer);
    }

    @Override
    public boolean shouldRenderOffScreen(PlungerHarpoonBlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(PlungerHarpoonBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }
}
