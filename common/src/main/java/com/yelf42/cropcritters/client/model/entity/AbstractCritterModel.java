package com.yelf42.cropcritters.client.model.entity;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.entity.AbstractCropCritterEntity;

public class AbstractCritterModel extends DefaultedEntityGeoModel<AbstractCropCritterEntity> {
    public AbstractCritterModel(ResourceLocation identifier, boolean basicAnimation){
        super(identifier);
        if (basicAnimation) withAltAnimations(ResourceLocation.fromNamespaceAndPath(CropCritters.MOD_ID, "basic_critter"));
    }
}
