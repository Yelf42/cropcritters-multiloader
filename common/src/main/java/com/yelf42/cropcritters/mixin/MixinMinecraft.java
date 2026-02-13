package com.yelf42.cropcritters.mixin;

import com.yelf42.cropcritters.CropCritters;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        
        CropCritters.LOGGER.info("This line is printed by an example mod common mixin!");
        CropCritters.LOGGER.info("MC Version: {}", Minecraft.getInstance().getVersionType());
    }
}