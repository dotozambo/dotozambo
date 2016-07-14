package com.dotozambo.Model;

import java.util.Map;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class HitterRecord {
	
	private final String date;			//CAHR(8)	: 20160712
	private final String team_code; 	//CHAR(2)	: HH
	private final String name;			//CHAR(30)	: Max 30
	private final String stadium;		//CHAR(4)	: home/away
	private final int order;			//INTEGER
	private final String position;		//CHAR(15)
	private final int num;				//INTEGER
	private final String record;		//STRING
	private final int ab;				//INTEGER
	private final int h;				//INTEGER
	private final int rbi;				//INTEGER
	private final int r;				//INTEGER
	private final float avg;			//FLOAT
	
	public HitterRecord(Map<String, Object> hitter, String date, String team_code, String stadium) 
	{
		this.date = date;
		this.team_code = team_code;
		this.stadium = stadium;
		
		this.name = (String) hitter.get("name");
		this.record = (String) hitter.get("record");
		
		this.num = (int) hitter.get("num");
		
		this.order = Integer.parseInt((String) hitter.get("order"));
		
		this.position = (String) hitter.get("position");
		this.ab = Integer.parseInt((String) hitter.get("ab"));
		this.h = Integer.parseInt((String) hitter.get("h"));
		this.rbi = Integer.parseInt((String) hitter.get("rbi"));
		this.r = Integer.parseInt((String) hitter.get("r"));
		
		float tmpAvg = 0;
		try {
			tmpAvg = Float.parseFloat((String) hitter.get("r"));
		} catch (Exception e) {/*PASS "-"*/}
		this.avg = tmpAvg;
	}
}
