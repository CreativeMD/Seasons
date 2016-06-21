package com.creativemd.seasons.season;

public class SeasonState {
	
	public final Season season;
	public final int days;
	/**0.5 (0 %) --> 1 (25 %) --> 1 (75 %) --> 0.5 (100 %)**/
	public final float percentage;
	
	public SeasonState(Season season, int days, float percentage) {
		this.season = season;
		this.days = days;
		this.percentage = percentage;
	}
	
}
