package com.creativemd.seasons.season;

public class SeasonState {
	
	public final Season season;
	public final int days;
	/**0.5 (0 %) --> 1 (25 %) --> 1 (75 %) --> 0.5 (100 %)**/
	public final float intensity;
	
	public final float percentage;
	
	
	public SeasonState(Season season, int days, float intensity) {
		this.season = season;
		this.days = days;
		this.intensity = intensity;
		this.percentage = days/season.duration;
	}
	
}
