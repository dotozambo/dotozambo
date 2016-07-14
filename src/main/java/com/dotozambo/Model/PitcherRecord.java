package com.dotozambo.Model;

import java.util.Map;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PitcherRecord {
	
	private final String date;			//CAHR(8)	: 20160712
	private final String team_code; 	//CHAR(2)	: HH
	private final String name;			//CHAR(30)	: Max 30
	private final String stadium;		//CHAR(4)	: home/away
	private final String si;			//CHAR(15)
	private final String result;		//CHAR(15)
	private final int w;				//INTEGER
	private final int l;				//INTEGER
	private final int sv;				//INTEGER
	private final String ip;			//CHAR(4)
	private final int tbf;				//INTEGER
	private final int np;
	private final int pa;
	private final int h;
	private final int hr;
	private final int bb;
	private final int so;
	private final int r;
	private final int er;
	private final float era;			//FLOAT
	
	public PitcherRecord(Map<String, Object> pitcher, String date, String team_code, String stadium) 
	{
		this.date = date;
		this.team_code = team_code;
		this.stadium = stadium;
		
		this.name = (String) pitcher.get("name");
		this.si = (String) pitcher.get("si");
		this.result = (String) pitcher.get("result");
		this.ip = (String) pitcher.get("ip");
		
		this.w = Integer.parseInt((String) pitcher.get("w"));
		this.l = Integer.parseInt((String) pitcher.get("l"));
		this.sv = Integer.parseInt((String) pitcher.get("sv"));
		this.tbf = Integer.parseInt((String) pitcher.get("tbf"));
		this.np = Integer.parseInt((String) pitcher.get("np"));
		this.pa = Integer.parseInt((String) pitcher.get("pa"));
		this.h = Integer.parseInt((String) pitcher.get("h"));
		this.hr = Integer.parseInt((String) pitcher.get("hr"));
		this.bb = Integer.parseInt((String) pitcher.get("bb"));
		this.so = Integer.parseInt((String) pitcher.get("so"));
		this.r = Integer.parseInt((String) pitcher.get("r"));
		this.er = Integer.parseInt((String) pitcher.get("er"));
		
		float tmpEra = 0;
		try {
			tmpEra = Float.parseFloat((String) pitcher.get("era"));
		} catch (Exception e) {/*PASS "-"*/}
		this.era = tmpEra;
	}
}
