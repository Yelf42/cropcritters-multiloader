package com.yelf42.cropcritters.mixin;

import net.minecraft.world.entity.LightningBolt;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.yelf42.cropcritters.blocks.MazewoodSaplingBlockEntity;
import com.yelf42.cropcritters.registry.ModBlocks;
import com.yelf42.cropcritters.events.WeedGrowNotifier;

import java.util.ArrayList;

import static net.minecraft.world.level.block.Block.pushEntitiesUp;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {

    @Shadow private boolean visualOnly;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LightningBolt;spawnFire(I)V", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        LightningBolt self = (LightningBolt) (Object)this;
        Vec3 pos = self.position();
        BlockPos blockPos = BlockPos.containing(pos.x, pos.y - 1.0E-6, pos.z);
        strikeCrops(self.level(), blockPos, self.getRandom());
    }

    @Unique
    private void strikeCrops(Level world, BlockPos pos, RandomSource random) {
        if (this.visualOnly) return;
        if (!(world instanceof ServerLevel)) return;

        ArrayList<BlockPos> checkLocations = new ArrayList<>();

        for(int i = 0; i < 3; ++i) {
            BlockPos rPos = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
            if (i == 0) rPos = pos;
            BlockState struckState = world.getBlockState(rPos);
            if (struckState.is(Blocks.FARMLAND)) {
                checkLocations.add(rPos.above());
                pushEntitiesUp(Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT.defaultBlockState(), world, rPos);
                world.setBlock(rPos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
            } else if (struckState.is(ModBlocks.SOUL_FARMLAND)) {
                checkLocations.add(rPos.above());
                pushEntitiesUp(ModBlocks.SOUL_FARMLAND.defaultBlockState(), Blocks.SOUL_SOIL.defaultBlockState(), world, rPos);
                world.setBlock(rPos, Blocks.SOUL_SOIL.defaultBlockState(), Block.UPDATE_CLIENTS);
            } else if (struckState.is(BlockTags.DIRT)) {
                checkLocations.add(rPos.above());
            }
        }

        if (checkLocations.isEmpty()) return;
        for (BlockPos posCrop : checkLocations) {
            BlockState toCheck = world.getBlockState(posCrop);
            if (toCheck.getBlock() instanceof CropBlock || toCheck.getBlock() instanceof StemBlock) {
                world.setBlock(posCrop, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                if (compareBlockPosXZ(posCrop, pos) && MazewoodSaplingBlockEntity.isWall(pos)) {
                    world.setBlock(posCrop, ModBlocks.MAZEWOOD_SAPLING.defaultBlockState(), Block.UPDATE_CLIENTS);
                    WeedGrowNotifier.notifyEvent(world, posCrop);
                }
            }
        }

    }

    @Unique
    private boolean compareBlockPosXZ(BlockPos a, BlockPos b) {
        return a.getX() == b.getX() && a.getZ() == b.getZ();
    }

}
