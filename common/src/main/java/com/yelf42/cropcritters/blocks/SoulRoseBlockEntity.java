package com.yelf42.cropcritters.blocks;

import com.yelf42.cropcritters.registry.ModBlockEntities;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import com.yelf42.cropcritters.area_affectors.AffectorType;
import com.yelf42.cropcritters.registry.ModEffects;
import com.yelf42.cropcritters.registry.ModParticles;

import java.util.List;
import java.util.function.Predicate;

public class SoulRoseBlockEntity extends BlockEntity {
    private static final Predicate<BlockState> CORE_MATERIALS = (blockState -> blockState.is(Blocks.RAW_COPPER_BLOCK) || blockState.is(Blocks.RAW_IRON_BLOCK) || blockState.is(Blocks.RAW_GOLD_BLOCK));

    // Central core
    public static final Vec3i[] STAGE_1A = new Vec3i[]{
            new Vec3i(0,-3,0),
            new Vec3i(0,-4,0),
            new Vec3i(0,-5,0)
    };
    public static final Vec3i[] STAGE_1B = new Vec3i[]{
            new Vec3i(1,-2,0),
            new Vec3i(2,-2,0),
            new Vec3i(3,-2,0),
    };

    public static final Vec3i[] STAGE_2 = new Vec3i[]{
            new Vec3i(4,-2,0),

            new Vec3i(4,-3,0),
            new Vec3i(4,-4,0),

            new Vec3i(4,-2,-1),
            new Vec3i(4,-2,-2),

            new Vec3i(4,-2,1),
            new Vec3i(4,-2,2),

            new Vec3i(5,-2,0),
            new Vec3i(6,-2,0),
            new Vec3i(7,-2,0)
    };

    public static final Vec3i[] STAGE_3 = new Vec3i[]{
            new Vec3i(8,-2,0),

            new Vec3i(8,-3,0),

            new Vec3i(8,-2,-1),

            new Vec3i(8,-2,1),

            new Vec3i(8,-2,0),
            new Vec3i(9,-2,0),
            new Vec3i(10,-2,0),
            new Vec3i(11,-2,0)
    };


    public SoulRoseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_ROSE, pos, state);
    }

    // Particles
    public static void clientTick(Level world, BlockPos pos, BlockState state, SoulRoseBlockEntity blockEntity) {
        int level = state.getOptionalValue(SoulRoseBlock.LEVEL).orElse(0);
        SoulRoseType type = state.getOptionalValue(SoulRoseBlock.TYPE).orElse(SoulRoseType.NONE);
        if (level == 0 || type == SoulRoseType.NONE) return;

        // Particles for being active
        if (world.getGameTime() % 20 == 0L) {
            if (world.isDay()) return;
            if (world.dimensionType().hasFixedTime() && !world.getBiome(pos).is(Biomes.SOUL_SAND_VALLEY)) return;

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            RandomSource random = world.random;
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();

            for(int l = 0; l < 2 * level; ++l) {
                double angle = random.nextDouble() * Math.PI * 2.0F;
                double radius = random.nextDouble() * levelToRadius(level) - 1.0F;
                mutable.set(i + radius * Math.sin(angle), j - random.nextInt(2), k + radius * Math.cos(angle));
                world.addParticle(ModParticles.SOUL_SIPHON, (double)mutable.getX() + random.nextDouble(), mutable.getY(), (double)mutable.getZ() + random.nextDouble(), (double)0.0F, (double)0.0F, (double)0.0F);
            }
        }

    }

    // Update Level + Copper Attack
    public static void serverTick(Level world, BlockPos pos, BlockState state, SoulRoseBlockEntity blockEntity) {
        // Update block level and type
        if (world.getGameTime() % 100L == 0L) {
            int lvl = updateLevel(world, pos);
            if (lvl != state.getOptionalValue(SoulRoseBlock.LEVEL).orElse(0)) {
                BlockState core = world.getBlockState(pos.offset(0, -2, 0));
                world.setBlockAndUpdate(pos, state.setValue(SoulRoseBlock.LEVEL, lvl).setValue(SoulRoseBlock.TYPE, SoulRoseType.getType(core, lvl)));
                if (SoulRoseBlock.isDoubleTallAtLevel(lvl)) {
                    world.setBlockAndUpdate(pos.above(), state.setValue(SoulRoseBlock.LEVEL, lvl).setValue(SoulRoseBlock.TYPE, SoulRoseType.getType(core, lvl)).setValue(SoulRoseBlock.HALF, DoubleBlockHalf.UPPER));
                } else {
                    if (world.getBlockState(pos.above()).is(ModBlocks.SOUL_ROSE)) world.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
                }
            }
        }

        int level = state.getOptionalValue(SoulRoseBlock.LEVEL).orElse(0);
        SoulRoseType type = state.getOptionalValue(SoulRoseBlock.TYPE).orElse(SoulRoseType.NONE);
        if (level == 0 || type == SoulRoseType.NONE) return;

        // Copper undead attack
        if (world.getGameTime() % 30 == 0L) {
            if (type == SoulRoseType.GOLD) {
                tryAttack((ServerLevel) world, pos, state, blockEntity);
            }
        }
    }

    private static void tryAttack(ServerLevel world, BlockPos pos, BlockState state, SoulRoseBlockEntity blockEntity) {
        int level = state.getOptionalValue(SoulRoseBlock.LEVEL).orElse(0);

        List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, getAttackZone(pos, level), (entity) -> entity.getType().is(EntityTypeTags.UNDEAD) && !entity.hasCustomName());
        for (LivingEntity livingEntity : list) {
            if (livingEntity == null) continue;
            if (livingEntity.hasEffect(ModEffects.SOUL_SIPHON)) continue;
            if (livingEntity.isAlive() && pos.closerThan(livingEntity.blockPosition(), levelToRadius(level))) {
                livingEntity.addEffect(new MobEffectInstance(ModEffects.SOUL_SIPHON, 320, level, false, true, false));
            }
        }

    }

    private static AABB getAttackZone(BlockPos pos, int level) {
        double xz = levelToRadius(level);
        return (new AABB(pos)).inflate(xz, 3, xz);
    }

    private static double levelToRadius(int level) {
        return (level == 3) ? AffectorType.SOUL_ROSE_COPPER_3.width : (level == 2) ? AffectorType.SOUL_ROSE_COPPER_2.width : AffectorType.SOUL_ROSE_COPPER_1.width;
    }

    private static int updateLevel(Level world, BlockPos pos) {
        if (!world.getBlockState(pos.above()).is(Blocks.AIR) && !world.getBlockState(pos.above()).is(ModBlocks.SOUL_ROSE)) return 0;

        BlockState core = world.getBlockState(pos.offset(0, -2, 0));
        Block coreBlock = core.getBlock();
        if (!CORE_MATERIALS.test(core)) return 0;
        int level = 0;

        // Stage 1
        for (Vec3i offset : STAGE_1A) {
            if (!world.getBlockState(pos.offset(offset)).is(coreBlock)) return level;
        }
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : STAGE_1B) {
                if (!world.getBlockState(pos.offset(rotate(offset, i))).is(coreBlock)) return level;
            }
        }
        level += 1;

        // Stage 2
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : STAGE_2) {
                if (!world.getBlockState(pos.offset(rotate(offset, i))).is(coreBlock)) return level;
            }
        }
        level += 1;

        // Stage 3
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : STAGE_3) {
                if (!world.getBlockState(pos.offset(rotate(offset, i))).is(coreBlock)) return level;
            }
        }

        return 3;
    }

    private static Vec3i rotate(Vec3i v, int dir) {
        int x = v.getX();
        int y = v.getY();
        int z = v.getZ();

        return switch (dir) {
            case 0  -> new Vec3i( x, y,  z);
            case 1 -> new Vec3i(-z, y,  x);
            case 2  -> new Vec3i(-x, y, -z);
            case 3 -> new Vec3i( z, y, -x);
            default -> v;
        };
    }
}
