package com.yelf42.cropcritters.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.entity.AbstractCropCritterEntity;
import com.yelf42.cropcritters.client.model.entity.AbstractCritterModel;
import software.bernie.geckolib.renderer.layer.ItemInHandGeoLayer;

public class AbstractCritterRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AbstractCropCritterEntity, R> {
    public static final DataTicket<ResourceLocation> TEXTURE_PATH = DataTicket.create("critter_texture_path", ResourceLocation.class);

    private final ResourceLocation texture;
    private final ResourceLocation trustingTexture;

    public AbstractCritterRenderer(EntityRendererProvider.Context context, ResourceLocation id, boolean basicAnimation) {
        super(context, new AbstractCritterModel(id, basicAnimation));
        this.texture = ResourceLocation.fromNamespaceAndPath(CropCritters.MOD_ID,"textures/entity/critters/" + id.getPath() + ".png");
        this.trustingTexture = ResourceLocation.fromNamespaceAndPath(CropCritters.MOD_ID,"textures/entity/critters/" + id.getPath() + "_trusting.png");
        if (id.getPath().equals("cocoa_critter")) {
            //addRenderLayer(new ItemInHandGeoLayer<>(this));
            addRenderLayer(new ItemInHandGeoLayer<>(this));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(R renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE_PATH, texture);
    }

    @Override
    public void extractRenderState(AbstractCropCritterEntity entity, R entityRenderState, float partialTick) {
        super.extractRenderState(entity, entityRenderState, partialTick);
        entityRenderState.addGeckolibData(TEXTURE_PATH, entity.isTrusting() ? trustingTexture : texture);
        entityRenderState.addGeckolibData(DataTickets.IS_SHAKING, entity.isShaking());
    }

//    @Override
//    public void addRenderData(AbstractCropCritterEntity animatable, Void relatedObject, R renderState) {
//        super.addRenderData(animatable, relatedObject, renderState);
//        renderState.addGeckolibData(TEXTURE_PATH, animatable.isTrusting() ? trustingTexture : texture);
//    }
}
