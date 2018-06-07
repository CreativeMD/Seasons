package com.creativemd.seasons.season;

import java.util.ArrayList;
import java.util.HashMap;

import com.creativemd.creativecore.utils.Graph;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Season {
	
	private static ArrayList<Season> seasons = new ArrayList<>();
	
	public static HashMap<Integer, Long> worldDays = new HashMap<>();
	//public static int currentWorldDays = 0;
	public static int defaultSeasonDuration = 20;
	
	static{
		//Default seasons for now
		seasons.add(new WinterSeason());
		seasons.add(new SpringSeason());
	}
	
	public static void incDay(World world)
	{
		Integer dim = new Integer(world.provider.getDimension());
		worldDays.put(dim, worldDays.get(dim).longValue()+1L);
		System.out.println("Day over! days=" + worldDays.get(dim));
	}
	
	public static long getCurrentDays(World world)
	{
		return getCurrentDays(world.provider.getDimension());
	}
	
	public static long getCurrentDays(int dim)
	{
		Long days = worldDays.get(dim);
		if(days == null)
		{
			days = new Long(0);
			worldDays.put(dim, days);
		}
		return days;
	}
	
	public static int getDurationOfYear()
	{
		int days = 0;
		for (int i = 0; i < seasons.size(); i++) {
			days += seasons.get(i).duration;
		}
		return days;
	}
	
	public static int getDayInYear(World world)
	{
		return (int) (getCurrentDays(world) % getDurationOfYear());
	}
	
	@SideOnly(Side.CLIENT)
	public static SeasonState getCurrentStateInClientWorld()
	{
		return getCurrentState(Minecraft.getMinecraft().world);
	}
	
	public static SeasonState getCurrentState(World world)
	{
		int days = 0;
		int currentDays = getDayInYear(world);
		for (int i = 0; i < seasons.size(); i++) {
			if(currentDays >= days && currentDays < days+seasons.get(i).duration)
				return seasons.get(i).getSeasonState(currentDays - days);
			days += seasons.get(i).duration;
		}
		return new SeasonState(seasons.get(0), 0, 0.5F);
	}
	
	/**in days**/
	public int duration;
	
	protected final HashMap<Block, BurnProperties> specialBurnProperties = new HashMap<>();
	protected final Graph intensityGraph;
	
	public final String unlocalizedName;
	
	public Season(String unlocalizedName, int duration) {
		this.unlocalizedName = unlocalizedName;
		this.duration = duration;
		fillSpecialBurnProperties(specialBurnProperties);
		this.intensityGraph = getIntensityGraph();
	}
	
	public SeasonState getSeasonState(int days)
	{
		float percentage = days/(float)this.duration;
		
		/*float intensity = days / (duration/4);
		if(intensity > 3)
			intensity = (1-(intensity-3))*0.5F+0.5F;
		else
			intensity = intensity*0.5F+0.5F;*/
		
		return new SeasonState(this, days, intensityGraph.getY(percentage));
	}
	
	public abstract float getTemperatureOffset(SeasonState state);
	
	public float getTemperature(SeasonState state, float original, BlockPos pos, Biome biome)
	{
		return original + getTemperatureOffset(state);
	}
	
	/**default is 3**/
	public abstract int getRandomTickSpeed(SeasonState state, int defaultTickSpeed);
	
	public abstract void onGrassColor(BiomeEvent.GetGrassColor event, SeasonState state);
	
	public abstract void onWaterColor(BiomeEvent.GetWaterColor event, SeasonState state);
	
	public abstract void onFoliageColor(BiomeEvent.GetFoliageColor event, SeasonState state);
	
	public abstract void fillSpecialBurnProperties(HashMap<Block, BurnProperties> specialBurnProperties);
	
	public abstract Graph getIntensityGraph();
	
	public HashMap<Block, BurnProperties> getSpecialBurnProperties(SeasonState state, float temperature, Biome biome)
	{
		return specialBurnProperties;
	}
	
	public static class BurnProperties {
		
		public final int encouragement;
		public final int flammability;
		public final Block result;
		
		public BurnProperties(int encouragement, int flammability) {
			this(encouragement, flammability, null);
		}
		
		public BurnProperties(int encouragement, int flammability, Block result) {
			this.encouragement = encouragement;
			this.flammability = flammability;
			this.result = result;
		}
		
	}
	
}