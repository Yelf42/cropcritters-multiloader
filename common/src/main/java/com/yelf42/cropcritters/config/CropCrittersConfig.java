package com.yelf42.cropcritters.config;

public class CropCrittersConfig {
    public int regularWeedChance = 3;
    public int netherWeedChance = 4;
    public boolean monoculturePenalize = true;
    public int lostSoulDropChance = 6;

    public int mazewoodSpread = 4;

    public int critterSpawnChance = 8;
    public double critterWorkSpeedMultiplier = 1.0;

    public boolean deadCoralGeneration = true;
    public boolean soulRoseHintGeneration = true;
    public boolean thornweedGeneration = true;
    public boolean waftgrassGeneration = true;
    public boolean spiteweedGeneration = true;
    public boolean strangleFernGeneration = true;
    public boolean puffbombGeneration = true;
    public boolean liverwortGeneration = true;

    public int goldSoulRoseSlowdown = 25;

    public static CropCrittersConfig getDefaults() {
        return new CropCrittersConfig();
    }

}
