package com.yelf42.cropcritters.client.renderer.blockentity;

import com.yelf42.cropcritters.registry.ModItems;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.Material;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.blocks.SoulPotBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SoulPotBlockEntityRenderer implements BlockEntityRenderer<SoulPotBlockEntity, SoulPotBlockEntityRenderState> {
    private static final Map<Integer, Material> COVER_SPRITES = new HashMap<>();
    private static final Map<Integer, Material> INSIDE_SPRITES = new HashMap<>();

    private final MaterialSet materials;
    private final ModelPart neck;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;
    private final ModelPart bottom;

    public SoulPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet(), context.materials());
    }

    public SoulPotBlockEntityRenderer(SpecialModelRenderer.BakingContext context) {
        this(context.entityModelSet(), context.materials());
    }

    public SoulPotBlockEntityRenderer(EntityModelSet entityModelSet, MaterialSet materials) {
        this.materials = materials;
        ModelPart modelPart = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild("neck");
        this.top = modelPart.getChild("top");
        this.bottom = modelPart.getChild("bottom");
        ModelPart modelPart2 = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.front = modelPart2.getChild("front");
        this.back = modelPart2.getChild("back");
        this.left = modelPart2.getChild("left");
        this.right = modelPart2.getChild("right");
    }

    public SoulPotBlockEntityRenderState createRenderState() {
        return new SoulPotBlockEntityRenderState();
    }

    public void extractRenderState(SoulPotBlockEntity soulPotBlockEntity, SoulPotBlockEntityRenderState soulPotBlockEntityRenderState, float f, Vec3 vec3d, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlayCommand) {
        BlockEntityRenderer.super.extractRenderState(soulPotBlockEntity, soulPotBlockEntityRenderState, f, vec3d, crumblingOverlayCommand);
        soulPotBlockEntityRenderState.facing = soulPotBlockEntity.getHorizontalFacing();
        SoulPotBlockEntity.WobbleType wobbleType = soulPotBlockEntity.lastWobbleType;
        if (wobbleType != null && soulPotBlockEntity.getLevel() != null) {
            soulPotBlockEntityRenderState.wobbleAnimationProgress = ((float)(soulPotBlockEntity.getLevel().getGameTime() - soulPotBlockEntity.lastWobbleTime) + f) / (float)wobbleType.lengthInTicks;
        } else {
            soulPotBlockEntityRenderState.wobbleAnimationProgress = 0.0F;
        }
        soulPotBlockEntityRenderState.level = soulPotBlockEntity.getTheItem().is(ModItems.LOST_SOUL) ? Math.clamp(soulPotBlockEntity.getTheItem().getCount() / 2, 0, 12) : 0;
    }

    public void submit(SoulPotBlockEntityRenderState soulPotBlockEntityRenderState, PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        matrixStack.pushPose();
        Direction direction = soulPotBlockEntityRenderState.facing;
        matrixStack.translate(0.5F, 0.0F, (double)0.5F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
        matrixStack.translate(-0.5F, 0.0F, (double)-0.5F);
        if (soulPotBlockEntityRenderState.wobbleAnimationProgress >= 0.0F && soulPotBlockEntityRenderState.wobbleAnimationProgress <= 1.0F) {
            if (soulPotBlockEntityRenderState.wobbleType == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                float g = soulPotBlockEntityRenderState.wobbleAnimationProgress * ((float)Math.PI * 2F);
                float h = -1.5F * (Mth.cos(g) + 0.5F) * Mth.sin((double)(g / 2.0F));
                matrixStack.rotateAround(Axis.XP.rotation(h * 0.015625F), 0.5F, 0.0F, 0.5F);
                float i = Mth.sin(g);
                matrixStack.rotateAround(Axis.ZP.rotation(i * 0.015625F), 0.5F, 0.0F, 0.5F);
            } else {
                float f = Mth.sin(-soulPotBlockEntityRenderState.wobbleAnimationProgress * 3.0F * (float)Math.PI) * 0.125F;
                float g = 1.0F - soulPotBlockEntityRenderState.wobbleAnimationProgress;
                matrixStack.rotateAround(Axis.YP.rotation(f * g), 0.5F, 0.0F, 0.5F);
            }
        }

        this.render(matrixStack, orderedRenderCommandQueue, soulPotBlockEntityRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, soulPotBlockEntityRenderState.level);
        matrixStack.popPose();
    }

    public void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, int i, int level) {
        RenderType renderLayer = Sheets.DECORATED_POT_BASE.renderType(RenderTypes::entitySolid);
        TextureAtlasSprite sprite = this.materials.get(Sheets.DECORATED_POT_BASE);
        queue.submitModelPart(this.neck, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, i);
        queue.submitModelPart(this.top, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, i);
        queue.submitModelPart(this.bottom, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, i);

        Material spriteIdentifier = COVER_SPRITES.get(level);
        TextureAtlasSprite stageSprite = this.materials.get(spriteIdentifier);
        RenderType coverLayer = spriteIdentifier.renderType(RenderTypes::entityCutout);
        queue.submitModelPart(this.front, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);
        queue.submitModelPart(this.back, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);
        queue.submitModelPart(this.left, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);
        queue.submitModelPart(this.right, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);

        if (level > 0) {
            Material spriteIdentifier2 = INSIDE_SPRITES.get(level);
            TextureAtlasSprite stageSprite2 = this.materials.get(spriteIdentifier2);
            RenderType insideLayer = spriteIdentifier2.renderType(RenderTypes::entityCutout);
            queue.submitModelPart(this.front, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
            queue.submitModelPart(this.back, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
            queue.submitModelPart(this.left, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
            queue.submitModelPart(this.right, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
        }
    }

    public void collectVertices(Consumer<Vector3fc> consumer) {
        PoseStack matrixStack = new PoseStack();
        this.neck.getExtentsForGui(matrixStack, consumer);
        this.top.getExtentsForGui(matrixStack, consumer);
        this.bottom.getExtentsForGui(matrixStack, consumer);
    }

    static {
        for (int i = 0; i < 13; i++) {
            COVER_SPRITES.put(i, new Material(
                    TextureAtlas.LOCATION_BLOCKS,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "block/soul_pot/soul_pot_cover_" + String.format("%02d", i))
            ));
            INSIDE_SPRITES.put(i, new Material(
                    TextureAtlas.LOCATION_BLOCKS,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "block/soul_pot/soul_pot_inside_" + String.format("%02d", i))
            ));
        }
    }
}