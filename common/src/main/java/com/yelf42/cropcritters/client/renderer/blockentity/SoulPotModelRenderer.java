package com.yelf42.cropcritters.client.renderer.blockentity;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public class SoulPotModelRenderer implements SpecialModelRenderer<Integer> {
    private final SoulPotBlockEntityRenderer blockEntityRenderer;

    public SoulPotModelRenderer(SoulPotBlockEntityRenderer blockEntityRenderer) {
        this.blockEntityRenderer = blockEntityRenderer;
    }

    @Override
    public @Nullable Integer extractArgument(ItemStack itemStack) {
        // Always return 0 for items since they're empty
        return 0;
    }

    @Override
    public void submit(@Nullable Integer level, ItemDisplayContext itemDisplayContext, PoseStack matrixStack,
                       SubmitNodeCollector orderedRenderCommandQueue, int light, int overlay, boolean bl, int k) {
        // Render with level 0 (or the provided level, which will always be 0)
        this.blockEntityRenderer.render(matrixStack, orderedRenderCommandQueue, light, overlay, k, level != null ? level : 0);
    }

    @Override
    public void getExtents(java.util.function.Consumer<Vector3fc> consumer) {
        this.blockEntityRenderer.collectVertices(consumer);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(BakingContext context) {
            return new SoulPotModelRenderer(new SoulPotBlockEntityRenderer(context));
        }
    }
}
