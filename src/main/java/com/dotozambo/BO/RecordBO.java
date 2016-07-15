package com.dotozambo.BO;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dotozambo.DAO.PitcherRecordDAO;

@Component
public class RecordBO {

	@Autowired
	URLCodecBO urlCodecBO;
	@Autowired
	PitcherRecordDAO pitcherRecordDAO;
	
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

}