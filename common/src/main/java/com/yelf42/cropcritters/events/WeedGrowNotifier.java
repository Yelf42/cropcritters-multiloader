package com.yelf42.cropcritters.events;

import com.yelf42.cropcritters.CropCritters;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WeedGrowNotifier {
    private static final Map<Level, Map<BlockPos, Long>> weedsToRing = new ConcurrentHashMap<>();
    private static final int RADIUS = 128;
    private static final long MAX_AGE_TICKS = 20 * 60;
    private static final long MAX_SIZE = 128;

    public static boolean checkWeedsToRing(Level world, BlockPos bellPos) {
        Map<BlockPos, Long> positions = weedsToRing.get(world);
        if (positions == null) return false;

        long currentTime = world.getGameTime();
        Iterator<Map.Entry<BlockPos, Long>> it = positions.entrySet().iterator();
        CropCritters.LOGGER.info(String.valueOf(positions.size()));
        while (it.hasNext()) {
            var entry = it.next();
            if (currentTime - entry.getValue() > MAX_AGE_TICKS) {
                it.remove();
                continue;
            }
            if (entry.getKey().closerThan(bellPos, RADIUS)) {
                it.remove();
                if (positions.isEmpty()) {
                    weedsToRing.remove(world);
                }
                return true;
            }
        }
        if (positions.isEmpty()) {
            weedsToRing.remove(world);
        }
        return false;
    }

    public static void notifyEvent(Level world, BlockPos eventPos) {
        weedsToRing.computeIfAbsent(world, w -> new ConcurrentHashMap<>());
        var positions = weedsToRing.get(world);
        if (positions.size() >= MAX_SIZE) {
            positions.clear();
        }
        positions.put(eventPos.immutable(), world.getGameTime());
    }

    public static void notifyRemoval(Level world, BlockPos eventPos) {
        Map<BlockPos, Long> positions = weedsToRing.get(world);
        if (positions == null) return;
        positions.remove(eventPos);
        if (positions.isEmpty()) {
            weedsToRing.remove(world);
        }
    }
}
