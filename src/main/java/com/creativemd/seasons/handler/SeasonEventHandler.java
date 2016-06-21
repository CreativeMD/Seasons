package com.creativemd.seasons.handler;

import com.creativemd.seasons.season.Season;
import com.creativemd.seasons.season.SeasonState;

import net.minecraft.block.Block;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SeasonEventHandler {
	
	@SubscribeEvent
	public void grassColor(BiomeEvent.GetGrassColor event)
	{
		SeasonState state = Season.getCurrentState();
		state.season.onGrassColor(event, state);
	}
	
	@SubscribeEvent
	public void waterColor(BiomeEvent.GetWaterColor event)
	{
		SeasonState state = Season.getCurrentState();
		state.season.onWaterColor(event, state);
	}
	
	@SubscribeEvent
	public void foliageColor(BiomeEvent.GetFoliageColor event)
	{
		SeasonState state = Season.getCurrentState();
		state.season.onFoliageColor(event, state);
	}
	
	
}
