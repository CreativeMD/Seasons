package com.creativemd.seasons.season;

import java.util.HashMap;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.utils.Graph;
import com.creativemd.creativecore.utils.LinearGraph;

import net.minecraft.block.Block;
import net.minecraftforge.event.terraingen.BiomeEvent.GetFoliageColor;
import net.minecraftforge.event.terraingen.BiomeEvent.GetGrassColor;
import net.minecraftforge.event.terraingen.BiomeEvent.GetWaterColor;

public class WinterSeason extends Season {

	public WinterSeason() {
		super("season.winter", defaultSeasonDuration);
	}

	@Override
	public float getTemperatureOffset(SeasonState state) {
		return -1.5F*state.intensity;
	}

	@Override
	public int getRandomTickSpeed(SeasonState state, int defaultTickSpeed) {
		return 0; //no ticks during winter
	}

	@Override
	public void onGrassColor(GetGrassColor event, SeasonState state) {
		event.setNewColor(ColorUtils.WHITE);
	}

	@Override
	public void onWaterColor(GetWaterColor event, SeasonState state) {
		event.setNewColor(ColorUtils.WHITE);
	}

	@Override
	public void onFoliageColor(GetFoliageColor event, SeasonState state) {
		event.setNewColor(ColorUtils.WHITE);
	}

	@Override
	public void fillSpecialBurnProperties(HashMap<Block, BurnProperties> specialBurnProperties) {
		
	}

	@Override
	public Graph getIntensityGraph() {
		HashMap<Float, Float> points = new HashMap<>();
		points.put(0F, 0.5F);
		points.put(0.25F, 1F);
		points.put(0.75F, 1F);
		points.put(1F, 0.5F);
		return new LinearGraph(points);
	}

}
