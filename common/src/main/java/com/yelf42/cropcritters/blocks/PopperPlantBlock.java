package com.yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import com.yelf42.cropcritters.registry.ModBlocks;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import com.yelf42.cropcritters.entity.PopperPodEntity;
import com.yelf42.cropcritters.events.WeedGrowNotifier;
import com.yelf42.cropcritters.registry.ModItems;

public class PopperPlantBlock extends VegetationBlock implements BonemealableBlock {

    public static final MapCodec<PopperPlantBlock> CODEC = simpleCodec(PopperPlantBlock::new);
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SHAPE = Block.column((double)8.0F, (double)0.0F, (double)13.0F);

    public PopperPlantBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(pos));
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.getValue(AGE);
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        int lightLevel = world.getBrightness(LightLayer.SKY, pos);
        float temp = world.getBiome(pos).value().getBaseTemperature();
        long time = world.getDayTime() % 24000;
        if (lightLevel < 14 || (time < 2000 || time > 9000) || (temp >= 1.0 || temp < 0.5)) return;

        if (!isMature(state)) {
            world.setBlockAndUpdate(pos, state.setValue(AGE, this.getAge(state) + 1));
        } else {
            if (random.nextInt(10) == 0) popOff(state, world, pos);
        }
    }

    private void popOff(BlockState state, ServerLevel world, BlockPos pos) {
        world.setBlockAndUpdate(pos, state.setValue(AGE, 0));

        Iterable<BlockPos> iterable = BlockPos.withinManhattan(pos, 12,1,12);
        for(BlockPos blockPos : iterable) {
            BlockState toCheck = world.getBlockState(blockPos);
            if (toCheck.is(ModBlocks.POPPER_PLANT) && isMature(toCheck)) {
                world.setBlockAndUpdate(blockPos, toCheck.setValue(AGE, 0));
                spawnPopperPod(world, toCheck, blockPos);
            }
        }

        spawnPopperPod(world, state, pos);
    }

    private void spawnPopperPod(ServerLevel world, BlockState state, BlockPos pos) {
        ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
        Vec3 center = pos.getCenter().add(state.getOffset(pos));
        Projectile.spawnProjectile(new PopperPodEntity(world, center.x, center.y, center.z, itemStack), world, itemStack);
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        super.onPlace(state, world, pos, oldState, notify);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        WeedGrowNotifier.notifyRemoval(world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextInt(2) == 0;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (!isMature(state)) {
            world.setBlockAndUpdate(pos, state.setValue(AGE, this.getAge(state) + 1));
        } else {
            popOff(state, world, pos);
        }
    }
}
