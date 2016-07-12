package com.dotozambo.Model;

import java.util.List;
import java.util.Map;


import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ScoreBoard {

	private final String date;
	
	private final String away_team;
	private final String away_score;
	private final int away_r;
	private final int away_h;
	private final int away_e;
	private final int away_b;
	
	private final String home_team;
	private final String home_score;
	private final int home_r;
	private final int home_h;
	private final int home_e;
	private final int home_b;
	
	@SuppressWarnings("unchecked")
	public ScoreBoard(Map<String, Object> map, String date) {
        
		Map<String, Object> awayMap = (Map<String, Object>) map.get("away");
		Map<String, Object> homeMap = (Map<String, Object>) map.get("home");
		
		this.date = date;
		
		this.away_team = (String) awayMap.get("away_team");
		String away_score = ((List<String>) awayMap.get("away_score")).toString();
		this.away_score = away_score.substring(1, away_score.length() - 1);
		this.away_r = Integer.parseInt((String) awayMap.get("away_r"));
		this.away_h = Integer.parseInt((String) awayMap.get("away_h"));
		this.away_e = Integer.parseInt((String) awayMap.get("away_e"));
		this.away_b = Integer.parseInt((String) awayMap.get("away_b"));
		
		this.home_team = (String) homeMap.get("home_team");
		String home_score = ((List<String>) homeMap.get("home_score")).toString();
		this.home_score = home_score.substring(1, away_score.length() - 1);
		this.home_r = Integer.parseInt((String) homeMap.get("home_r"));
		this.home_h = Integer.parseInt((String) homeMap.get("home_h"));
		this.home_e = Integer.parseInt((String) homeMap.get("home_e"));
		this.home_b = Integer.parseInt((String) homeMap.get("home_b"));
		
    }
}
