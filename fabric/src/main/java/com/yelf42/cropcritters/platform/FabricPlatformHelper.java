package com.yelf42.cropcritters.platform;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.platform.services.IPlatformHelper;
import com.yelf42.cropcritters.registry.ModItems;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public CreativeModeTab tabBuilder(String title, List<Item> items) {
        return FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.LOST_SOUL))
                .title(Component.translatable("itemGroup.cropcritters"))
                .displayItems((itemDisplayParameters, output) -> {
                    items.forEach(output::accept);
                }).build();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks) {
        return FabricBlockEntityTypeBuilder.create(function::apply, validBlocks).build();
    }

    @Override
    public SimpleParticleType simpleParticleType() {
        return FabricParticleTypes.simple();
    }

    @Override
    public java.nio.file.Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    // AFFECTORS
    public static final AttachmentType<AffectorPositions> AFFECTOR_POSITIONS_ATTACHMENT_TYPE =
            AttachmentRegistry.<AffectorPositions>builder()
                    .persistent(AffectorPositions.CODEC)
                    .buildAndRegister(CropCritters.identifier("affector_positions"));

    @Override
    public AffectorPositions getAffectorPositions(ServerLevel world) {
        return world.getAttachedOrElse(
                AFFECTOR_POSITIONS_ATTACHMENT_TYPE,
                AffectorPositions.EMPTY
        );
    }

    @Override
    public void setAffectorPositions(ServerLevel world, AffectorPositions positions) {
        world.setAttached(AFFECTOR_POSITIONS_ATTACHMENT_TYPE, positions);
    }
}
