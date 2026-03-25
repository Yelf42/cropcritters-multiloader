package com.yelf42.cropcritters.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yelf42.cropcritters.CropCritters;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModComponents {

    // ---- SEED TYPES ----

    public record SeedTypesComponent(List<ResourceLocation> seedTypes) {
        public static final Codec<SeedTypesComponent> CODEC = RecordCodecBuilder.create(builder ->
                builder.group(
                        ResourceLocation.CODEC.listOf().fieldOf("seedTypes").forGetter(SeedTypesComponent::seedTypes)
                ).apply(builder, SeedTypesComponent::new));

        public void addToTooltip(Consumer<Component> tooltip, TooltipFlag flag) {
            for (ResourceLocation seedType : seedTypes) {
                tooltip.accept(Component.literal(" - ").append(
                        Component.translatable("block." + seedType.getNamespace() + "." + seedType.getPath())
                                .withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    private static final String SEED_TYPES_KEY = "cropcritters_seed_types";

    public static SeedTypesComponent getSeedTypes(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SEED_TYPES_KEY)) return new SeedTypesComponent(Collections.emptyList());
        return SeedTypesComponent.CODEC.parse(NbtOps.INSTANCE, tag.get(SEED_TYPES_KEY))
                .resultOrPartial(e -> CropCritters.LOGGER.error("Failed to read seed_types: {}", e))
                .orElse(new SeedTypesComponent(Collections.emptyList()));
    }

    public static void setSeedTypes(ItemStack stack, SeedTypesComponent value) {
        SeedTypesComponent.CODEC.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(e -> CropCritters.LOGGER.error("Failed to write seed_types: {}", e))
                .ifPresent(nbt -> stack.getOrCreateTag().put(SEED_TYPES_KEY, nbt));
    }

    // ---- POISONOUS SEED BALL ----

    public record PoisonousComponent(int poisonStacks) {
        public static final Codec<PoisonousComponent> CODEC = RecordCodecBuilder.create(builder ->
                builder.group(
                        Codec.INT.fieldOf("poisonStacks").forGetter(PoisonousComponent::poisonStacks)
                ).apply(builder, PoisonousComponent::new));

        public void addToTooltip(Consumer<Component> tooltip, TooltipFlag flag) {
            if (poisonStacks > 0) tooltip.accept(
                    Component.translatable("item.cropcritters.tooltip.poisonous_seed_ball")
                            .append(CropCritters.INT_TO_ROMAN[poisonStacks])
                            .withStyle(ChatFormatting.GREEN));
        }
    }

    private static final String POISONOUS_KEY = "cropcritters_poisonous_seed_ball";

    public static PoisonousComponent getPoisonous(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(POISONOUS_KEY)) return new PoisonousComponent(0);
        return PoisonousComponent.CODEC.parse(NbtOps.INSTANCE, tag.get(POISONOUS_KEY))
                .resultOrPartial(e -> CropCritters.LOGGER.error("Failed to read poisonous_seed_ball: {}", e))
                .orElse(new PoisonousComponent(0));
    }

    public static void setPoisonous(ItemStack stack, PoisonousComponent value) {
        PoisonousComponent.CODEC.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(e -> CropCritters.LOGGER.error("Failed to write poisonous_seed_ball: {}", e))
                .ifPresent(nbt -> stack.getOrCreateTag().put(POISONOUS_KEY, nbt));
    }

    // ---- NO REGISTRATION NEEDED ----
    // NBT keys don't need to be registered with any registry.
    // The bind() call in your mod setup can be removed entirely.
}