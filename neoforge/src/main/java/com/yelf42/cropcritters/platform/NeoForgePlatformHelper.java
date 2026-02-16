package com.yelf42.cropcritters.platform;

import com.yelf42.cropcritters.CropCritters;
import com.yelf42.cropcritters.area_affectors.AffectorPositions;
import com.yelf42.cropcritters.platform.services.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public Builder tabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> function, Block... validBlocks) {
        return new BlockEntityType<>(function::apply, Set.of(validBlocks));
    }

    @Override
    public SimpleParticleType simpleParticleType() {
        return new SimpleParticleType(false);
    }

    @Override
    public java.nio.file.Path getConfigPath() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    // AFFECTORS

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CropCritters.MOD_ID);
    public static final Supplier<AttachmentType<AffectorPositions>> AFFECTOR_POSITIONS_ATTACHMENT_TYPE =
            ATTACHMENT_TYPES.register(
                    "affector_positions",
                    () -> AttachmentType.builder(() -> AffectorPositions.EMPTY)
                            .serialize(AffectorPositions.CODEC.fieldOf("data"))
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

    @Override
    public AffectorPositions getAffectorPositions(ServerLevel world) {
        return world.getData(AFFECTOR_POSITIONS_ATTACHMENT_TYPE);
    }

    @Override
    public void setAffectorPositions(ServerLevel world, AffectorPositions positions) {
        world.setData(AFFECTOR_POSITIONS_ATTACHMENT_TYPE, positions);
    }
}