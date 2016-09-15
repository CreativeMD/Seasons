package com.creativemd.seasons.handler;

import java.util.HashMap;

import com.creativemd.seasons.season.Season;
import com.creativemd.seasons.season.SeasonState;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SeasonEventHandler {
	
	public HashMap<Integer, Long> lastWorldTime = new HashMap<>();
	
	public static World lastWorld;
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.START)
			lastWorld = event.player.worldObj;
		//else
			//lastWorld = null;
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			lastWorld = event.world;
			
			long currentWorldTime = event.world.getWorldTime() % 24000L;
			Long lastTime = lastWorldTime.get(event.world.provider.getDimension());
			if(lastTime != null && currentWorldTime < lastTime)
			{
				Season.incDay(event.world);
			}
			lastWorldTime.put(event.world.provider.getDimension(), currentWorldTime);
		}else
			lastWorld = null;
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void grassColor(BiomeEvent.GetGrassColor event)
	{
		SeasonState state = Season.getCurrentStateInClientWorld();
		state.season.onGrassColor(event, state);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void waterColor(BiomeEvent.GetWaterColor event)
	{
		SeasonState state = Season.getCurrentStateInClientWorld();
		state.season.onWaterColor(event, state);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void foliageColor(BiomeEvent.GetFoliageColor event)
	{
		SeasonState state = Season.getCurrentStateInClientWorld();
		state.season.onFoliageColor(event, state);
	}
	
	
}
