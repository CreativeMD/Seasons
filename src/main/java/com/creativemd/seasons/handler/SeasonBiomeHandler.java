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
	
private static NoiseGeneratorPerlin TEMPERATURE_NOISE = null;
    
    public static NoiseGeneratorPerlin getTemperatureNoise()
    {
    	if(TEMPERATURE_NOISE == null)
    		TEMPERATURE_NOISE = ReflectionHelper.getPrivateValue(Biome.class, null, "TEMPERATURE_NOISE");
    	return TEMPERATURE_NOISE;
    }
    
    public static float getFloatTemperature(Biome biome, BlockPos pos)
    {
    	SeasonState state = null;
    	if(FMLCommonHandler.instance().getEffectiveSide().isClient())
    		state = Season.getCurrentStateInClientWorld();
    	else
    		state = Season.getCurrentState(SeasonEventHandler.lastWorld);
    	if (pos.getY() > 64)
        {
            float f = (float)(getTemperatureNoise().getValue((double)((float)pos.getX() / 8.0F), (double)((float)pos.getZ() / 8.0F)) * 4.0D);
            return state.season.getTemperature(state, biome.getTemperature() - (f + (float)pos.getY() - 64.0F) * 0.05F / 30.0F, pos, biome);
        }
        else
        {
            return state.season.getTemperature(state, biome.getTemperature(), pos, biome);
        }
    }
    
}
