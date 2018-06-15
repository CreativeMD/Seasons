package com.creativemd.seasons.handler;

import com.creativemd.seasons.season.Season;
import com.creativemd.seasons.season.SeasonState;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.server.FMLServerHandler;

public class SeasonBiomeHandler {
    
    public static float getTemperature(Biome biome, BlockPos pos)
    {
    	SeasonState state = null;
    	if(FMLCommonHandler.instance().getEffectiveSide().isClient())
    		state = Season.getCurrentStateInClientWorld();
    	else
    		state = Season.getCurrentState(SeasonEventHandler.lastWorld);
    	
    	return state.season.getTemperature(state, biome.getTemperature(pos), pos, biome);
    }
    
}
