package com.yelf42.cropcritters.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import com.yelf42.cropcritters.CropCritters;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModComponents {

    // Item components
    public static final DataComponentType<SeedTypesComponent> SEED_TYPES = DataComponentType.<SeedTypesComponent>builder().persistent(SeedTypesComponent.CODEC).build();
    public record SeedTypesComponent(List<Identifier> seedTypes) implements TooltipProvider {
        public static final Codec<SeedTypesComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    Identifier.CODEC.listOf().fieldOf("seedTypes").forGetter(SeedTypesComponent::seedTypes)
            ).apply(builder, SeedTypesComponent::new);
        });

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag type, DataComponentGetter components) {
            for (Identifier seedType : seedTypes) {
                tooltip.accept(Component.literal(" - ").append(Component.translatable("block." + seedType.getNamespace() + "." + seedType.getPath()).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    public static final DataComponentType<PoisonousComponent> POISONOUS_SEED_BALL = DataComponentType.<PoisonousComponent>builder().persistent(PoisonousComponent.CODEC).build();
    public record PoisonousComponent(int poisonStacks) implements TooltipProvider {
        public static final Codec<PoisonousComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    Codec.INT.fieldOf("poisonStacks").forGetter(PoisonousComponent::poisonStacks)
            ).apply(builder, PoisonousComponent::new);
        });

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag type, DataComponentGetter components) {
            if (poisonStacks > 0) tooltip.accept(Component.translatable("item.cropcritters.tooltip.poisonous_seed_ball").append(CropCritters.INT_TO_ROMAN[poisonStacks]).withStyle(ChatFormatting.GREEN));
        }
    }

    /// BINDER
    public static void register(BiConsumer<DataComponentType<?>, Identifier> consumer) {
        consumer.accept(SEED_TYPES, CropCritters.identifier("seed_types"));
        consumer.accept(POISONOUS_SEED_BALL, CropCritters.identifier("poisonous_seed_ball"));
    }

}
