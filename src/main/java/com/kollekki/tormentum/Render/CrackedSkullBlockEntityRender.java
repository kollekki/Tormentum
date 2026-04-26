package com.kollekki.tormentum.Render;

import com.kollekki.tormentum.BlockEntities.CrackedSkullBlockEntity;
import com.kollekki.tormentum.Tormentum;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CrackedSkullBlockEntityRender implements BlockEntityRenderer<CrackedSkullBlockEntity, CrackedSkullRenderState> {

    private final ItemModelResolver itemModelResolver;

    public CrackedSkullBlockEntityRender(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public CrackedSkullRenderState createRenderState() {
        return new CrackedSkullRenderState();
    }

    @Override
    public void extractRenderState(CrackedSkullBlockEntity blockEntity, CrackedSkullRenderState renderState, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.heldItem = blockEntity.getHeldItem();
        renderState.gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        renderState.partialTick = partialTick;

        if (!renderState.heldItem.isEmpty()) {
            renderState.itemRenderState.clear();
            this.itemModelResolver.updateForTopItem(
                    renderState.itemRenderState,
                    renderState.heldItem,
                    ItemDisplayContext.FIXED,
                    null,
                    null,
                    0
            );
        }
    }

    @Override
    public void submit(CrackedSkullRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (renderState.heldItem.isEmpty()) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.75, 0.5);

        float bob = Mth.sin((renderState.gameTime + renderState.partialTick) / 10.0F) * 0.075F;
        poseStack.translate(0, bob, 0);

        float rotation = (renderState.gameTime + renderState.partialTick) * 2.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(0.5F, 0.5F, 0.5F);

        renderState.itemRenderState.submit(
                poseStack,
                collector,
                0xF000F0,
                0xA0000,
                0
        );

        poseStack.popPose();
    }
}