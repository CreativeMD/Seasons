package com.creativemd.seasons.season;

import java.util.HashMap;

import com.creativemd.creativecore.utils.Graph;
import com.creativemd.creativecore.utils.LinearGraph;

import net.minecraft.block.Block;
import net.minecraftforge.event.terraingen.BiomeEvent.GetFoliageColor;
import net.minecraftforge.event.terraingen.BiomeEvent.GetGrassColor;
import net.minecraftforge.event.terraingen.BiomeEvent.GetWaterColor;

public class SpringSeason extends Season {

	public SpringSeason() {
		super("season.spring", defaultSeasonDuration);
	}

	@Override
	public float getTemperatureOffset(SeasonState state) {
		return 0;
	}

	@Override
	public int getRandomTickSpeed(SeasonState state, int defaultTickSpeed) {
		return 5;
	}

	@Override
	public void onGrassColor(GetGrassColor event, SeasonState state) {
		
	}

	@Override
	public void onWaterColor(GetWaterColor event, SeasonState state) {
		
	}

	@Override
	public void onFoliageColor(GetFoliageColor event, SeasonState state) {
		
	}

	@Override
	public void fillSpecialBurnProperties(HashMap<Block, BurnProperties> specialBurnProperties) {
		
	}

	@Override
	public Graph getIntensityGraph() {
		HashMap<Float, Float> points = new HashMap<>();
		points.put(0F, 0.2F);
		points.put(0.25F, 0.6F);
		points.put(0.75F, 1F);
		return new LinearGraph(points);
	}

}
