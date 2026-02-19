package com.yelf42.cropcritters.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
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
            addRenderLayer(new BlockAndItemGeoLayer<>(this) {
                @Override
                protected @Nullable ItemStack getStackForBone(GeoBone bone, AbstractCropCritterEntity animatable) {
                    if ((bone.getName()).equals("RightHandItem")) {
                        return animatable.getMainHandItem();
                    }
                    return null;
                }

                @Override
                protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, AbstractCropCritterEntity animatable) {
                    return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                }
            });
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractCropCritterEntity entity) {
        return entity.isTrusting() ? trustingTexture : texture;
    }
}
