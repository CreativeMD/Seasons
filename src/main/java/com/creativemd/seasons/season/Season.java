package com.creativemd.seasons.season;

import java.util.ArrayList;

import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public abstract class Season {
	
	private static ArrayList<Season> seasons = new ArrayList<>();
	
	public static int currentWorldDays = 0;
	public static int defaultSeasonDuration = 20;
	
	static{
		//Default seasons for now
		seasons.add(new WinterSeason());
	}
	
	public static int getDurationOfYear()
	{
		int days = 0;
		for (int i = 0; i < seasons.size(); i++) {
			days += seasons.get(i).duration;
		}
		return days;
	}
	
	public static int getDayInYear()
	{
		return currentWorldDays % getDurationOfYear();
	}
	
	public static SeasonState getCurrentState()
	{
		int days = 0;
		int currentDays = getDayInYear();
		for (int i = 0; i < seasons.size(); i++) {
			if(days >= currentDays && currentDays < days+seasons.get(i).duration)
				return seasons.get(i).getSeasonState(currentDays - days);
			days += seasons.get(i).duration;
		}
		return new SeasonState(seasons.get(0), 0, 0.5F);
	}
	
	/**in days**/
	public int duration;
	
	public final String unlocalizedName;
	
	public Season(String unlocalizedName, int duration) {
		this.unlocalizedName = unlocalizedName;
		this.duration = duration;
	}
	
	public SeasonState getSeasonState(int days)
	{
		float percentage = days / (duration/4);
		if(percentage > 3)
			percentage = (1-(percentage-3))*0.5F+0.5F;
		else
			percentage = percentage*0.5F+0.5F;
		return new SeasonState(this, days, Math.max(1, percentage));
	}
	
	public abstract float getTemperatureOffset(SeasonState state);
	
	/**default is 3**/
	public abstract int getRandomTickSpeed(SeasonState state);
	
	public abstract void onGrassColor(BiomeEvent.GetGrassColor event, SeasonState state);
	
	public abstract void onWaterColor(BiomeEvent.GetWaterColor event, SeasonState state);
	
	public abstract void onFoliageColor(BiomeEvent.GetFoliageColor event, SeasonState state);
	
}