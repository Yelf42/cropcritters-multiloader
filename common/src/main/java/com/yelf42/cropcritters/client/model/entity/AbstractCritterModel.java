package com.yelf42.cropcritters.client.model.entity;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.entity.AbstractCropCritterEntity;

public class AbstractCritterModel extends DefaultedEntityGeoModel<AbstractCropCritterEntity> {
    public AbstractCritterModel(Identifier identifier, boolean basicAnimation){
        super(identifier);
        if (basicAnimation) withAltAnimations(Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "basic_critter"));
    }
}
