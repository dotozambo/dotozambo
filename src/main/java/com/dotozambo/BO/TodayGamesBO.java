package com.dotozambo.BO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TodayGamesBO {

	@Autowired
	CommonBO commonBO;
	
	public List<Map <String, String>> getTodayGamesMap(String inputday) throws IOException {
		
		String naverUrl = "http://sports.news.naver.com/kbaseball/index.nhn";
		String res = commonBO.sendGet(naverUrl);
		Document doc = Jsoup.parse(res);
		
		Element scheduleBox = doc.getElementById("_schedule_box");
		String date = scheduleBox.getElementsByClass("day").get(0).text();
		Elements games = scheduleBox.getElementsByClass("hmb_list_items");
		
		String _date = commonBO.dateFormat(date, 2016);
		
		if (!_date.equals(inputday)) return null;

		List<Map <String, String>> todayGameMap = new ArrayList<Map<String, String>>();		
		for (Element _div : games) 
		{
			Elements detail = _div.getElementsByClass("inner");
			
			Element awayDiv = detail.get(0);
			String away_team = awayDiv.text().split(" ")[0];
			String away_starter;
			
			
			if (awayDiv.text().split(" ").length == 1) {
				away_starter = "몰라";
			}
			else {
				away_starter = awayDiv.text().split(" ")[1];
			}
			
			Element homeDiv = detail.get(1);
			String home_team = homeDiv.text().split(" ")[0];
			String home_starter;
			
			
			if (homeDiv.text().split(" ").length == 1){
				home_starter = "몰라";
			}
			else {
				home_starter = homeDiv.text().split(" ")[1];
			}
			
			Element timeDiv = detail.get(2);
			
			Map <String, String> gameMap = new HashMap<>();
			gameMap.put("away_team", away_team);
			gameMap.put("away_starter", away_starter);
			gameMap.put("home_team", home_team);
			gameMap.put("home_starter", home_starter);
			gameMap.put("time", timeDiv.text());
			gameMap.put("date", inputday);
			todayGameMap.add(gameMap);
		}
		return todayGameMap;
	}
}
