package com.dotozambo.BO;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dotozambo.DAO.PitcherRecordDAO;
import com.dotozambo.DAO.ScoreBoardDAO;

@Component
public class RecordBO {

	@Autowired
	URLCodecBO urlCodecBO;
	
	@Autowired
	PitcherRecordDAO pitcherRecordDAO;
	@Autowired
	ScoreBoardDAO scoreBoardDAO;
	
	static String AWAY = "어웨이"; 	/*어웨이*/
	static String HOME = "홈";						/*홈*/
	
	static String PITCHER = "투수"; 			/*투수*/
	static String HITTER = "타자"; 			/*타자*/
	
	//Pitcher Record
	static String ERA = "방어율";
	//Hitter Record
	static String H = "타율";
	
	//caseNum : 0 - [POS] [NAME] [RECORD]
	//caseNum : 1 - [POS] [NAME] (STADIUM) [RECORD]
	public String getRecord(Map<String, String> queryParam) throws UnsupportedEncodingException {
		
		String tableName = new String();
		if (queryParam.get("POS").equals(HITTER))
			tableName = "hitterrecord";
		else
			tableName = "pitcherrecord";
		
		String name = queryParam.get("NAME");
		String record = queryParam.get("RECORD");
		
		String sql = "SELECT %s FROM %s WHERE [name] = '%s' ORDER BY [date] DESC LIMIT %d";
		sql = String.format(sql, record, tableName, urlCodecBO.encode(name), 1);
		
		String result = pitcherRecordDAO.selectPitcherRecord(sql);
		return result;
	}
	
	public Map <String, Object> getRPitcherRecordMap(String team_code, int game_num) throws UnsupportedEncodingException 
	{
		
		Date today = new Date();
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
		String todayStr = dateFormater.format(today);
		
		Map <String, Object> return_pitcherMap = new HashMap<String, Object>();
		Map <String, String> resultMap = scoreBoardDAO.selectLatestGameScoreBoard(team_code, game_num);
		for (String date : resultMap.keySet()) 
		{
			int fatigued = getDiffDay(todayStr, date) + (game_num + 1);
			if (fatigued < 1) fatigued = 1;
			
			List <Map <String, Object>> pitchers = pitcherRecordDAO.selectAllPitcherRecord(date, team_code);
			for (Map <String, Object> pitcher : pitchers) 
			{
				if (((String) pitcher.get("si")).equals("%EC%84%A0%EB%B0")) continue; //선발
				
				String pitcherName = (String) pitcher.get("name");
				if (return_pitcherMap.get(pitcherName) == null) 
				{
					Map <String, Object> pitcherRecordMap = new HashMap <String, Object>();
					String ipStr = (String) pitcher.get("ip");
					pitcherRecordMap.put("ip",  ipString2DoubleStr(ipStr));
					pitcherRecordMap.put("npr", ((int) pitcher.get("np") * fatigued));
					pitcherRecordMap.put("er", ((int) pitcher.get("er") * fatigued));
					return_pitcherMap.put(pitcherName, pitcherRecordMap);
				}
				else 
				{
					@SuppressWarnings("unchecked")
					Map <String, Object> pre_pitcherRecordMap = (Map<String, Object>) return_pitcherMap.get(pitcherName);
					double preIP = Double.parseDouble((String) pre_pitcherRecordMap.get("ip"))
									+ Double.parseDouble(ipString2DoubleStr((String) pitcher.get("ip")));
					int preNPR = (int) pre_pitcherRecordMap.get("npr") + ((int) pitcher.get("np") * fatigued);
					int preER = (int) pre_pitcherRecordMap.get("er") + ((int) pitcher.get("er") * fatigued);
					
					pre_pitcherRecordMap.put("ip", String.format("%.1f", preIP));
					pre_pitcherRecordMap.put("npr", preNPR);
					pre_pitcherRecordMap.put("er", preER);
					return_pitcherMap.put(pitcherName, pre_pitcherRecordMap);
				}
			}
		}
		return return_pitcherMap;
	}
	
	public String ipString2DoubleStr(String ip) {
		
		double _integer = 0;
		double _float = 0;
		if (ip.split(" ").length == 2) {
			//1 1/3
			_integer = Integer.parseInt(ip.split(" ")[0]);
			_float = Integer.parseInt(ip.split(" ")[1].split("/")[0]);
		}
		else {
			if (ip.split("/").length == 2){
				//1/3
				_float = Integer.parseInt(ip.split(" ")[0].split("/")[0]);
			}
			else {
				//1
				_integer = Integer.parseInt(ip.split(" ")[0].split("/")[0]);
			}
		}
		
		return String.format("%.1f", _integer + (_float / 10.0));
	}
	
	public int getDiffDay(String start, String end) {
		
		long diff = 0;
		long diffDays = 0;
		try 
		{
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	        Date beginDate = (Date) formatter.parse(start);
	        Date endDate = (Date) formatter.parse(end);
	         
	        // 시간차이를 시간,분,초를 곱한 값으로 나누면 하루 단위가 나옴
	        diff = endDate.getTime() - beginDate.getTime();
	        diffDays = diff / (24 * 60 * 60 * 1000);
	         
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
		
		return (int) diffDays;
	}
}
