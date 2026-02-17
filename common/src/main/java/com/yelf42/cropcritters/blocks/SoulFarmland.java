package com.yelf42.cropcritters.blocks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import com.yelf42.cropcritters.CropCritters;

public class SoulFarmland extends FarmBlock {

    public SoulFarmland(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 7));
    }

    public static void setToSoulSoil(@Nullable Entity entity, BlockState state, Level world, BlockPos pos) {
        BlockState blockState = pushEntitiesUp(state, Blocks.SOUL_SOIL.defaultBlockState(), world, pos);
        world.setBlockAndUpdate(pos, blockState);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, blockState));
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (world instanceof ServerLevel serverWorld) {
            if ((double)world.random.nextFloat() < fallDistance - (double)0.5F
                    && entity instanceof LivingEntity
                    && (entity instanceof Player || serverWorld.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
                    && (entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F || entity.getType().is(CropCritters.CROP_CRITTERS))) {
                setToSoulSoil(entity, state, world, pos);
            }
        }
        Blocks.SOUL_SOIL.fallOn(world, state, pos, entity, fallDistance);
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(world, pos)) {
            setToSoulSoil(null, state, world, pos);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return false;
    }
}
