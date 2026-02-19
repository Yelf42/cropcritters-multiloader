package com.yelf42.cropcritters.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.entity.AbstractCropCritterEntity;
import com.yelf42.cropcritters.client.model.entity.AbstractCritterModel;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class AbstractCritterRenderer
        extends GeoEntityRenderer<AbstractCropCritterEntity> {

    private final ResourceLocation texture;
    private final ResourceLocation trustingTexture;

    public AbstractCritterRenderer(
            EntityRendererProvider.Context context,
            ResourceLocation id,
            boolean basicAnimation
    ) {
        super(context, new AbstractCritterModel(id, basicAnimation));

        this.texture = CropCritters.identifier("textures/entity/critters/" + id.getPath() + ".png"
        );

        this.trustingTexture = CropCritters.identifier("textures/entity/critters/" + id.getPath() + "_trusting.png"
        );

        if (id.getPath().equals("cocoa_critter")) {
            addRenderLayer(new BlockAndItemGeoLayer<>(this));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractCropCritterEntity entity) {
        return entity.isTrusting() ? trustingTexture : texture;
    }
}
