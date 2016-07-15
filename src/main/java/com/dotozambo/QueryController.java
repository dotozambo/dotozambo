package com.dotozambo;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dotozambo.BO.RecordBO;

@Component
public class QueryController
{
	@Autowired
	private RecordBO recordBO;
	
	static String PITCHER = "투수"; 			/*투수*/
	static String HITTER = "타자"; 			/*타자*/
	
	public Map <String, Object> inputStr2QueryStr(String inputstring) throws UnsupportedEncodingException 
	{
		//(WHERE) ([PERIOD]:[GAME_NUM]) [POS] [NAME] (STADIUM) [RECORD]
		String [] inputStr = inputstring.split(" ");
		
		int length = inputStr.length;
		int cri = -1;
		int returnCode = 0;
		String retValue = new String();
		
		int defaultLength = 3;
		int caseNum = -1;
		
		Map <String, Object> resultMap = new HashMap<String, Object>();
		
		if (length < 3) {
			returnCode = 2; //Input Fail (Minimum 3)
			resultMap.put("case", caseNum);
			resultMap.put("code", returnCode);
			resultMap.put("retValue", "에러! 쿼리가 짧다..");
			return resultMap;
		}
		
		for (int i = 0; i < length; i++) {
			if (inputStr[i].equals(HITTER) 
					|| inputStr[i].equals(PITCHER)) {
				cri = i;
				break;
			}
		}
		
		if (cri < 0) {
			returnCode = 3; // POS Input Fail
			resultMap.put("case", caseNum);
			resultMap.put("code", returnCode);
			resultMap.put("retValue", "에러! 포지션은 '투수 or 타자' 만..");
			return resultMap;
		}

		Map <String, String> queryParam = new HashMap<String, String>();
		try
		{
			switch (cri) {
			//[POS] [NAME] (STADIUM) [RECORD]
			case 0:
				if (length == defaultLength) {
					//[POS] [NAME] [RECORD]
					queryParam.put("POS", inputStr[0]);
					queryParam.put("NAME", inputStr[1]);
					queryParam.put("RECORD", inputStr[2]);
					/////////////////////////////////////////////////////////
					caseNum = 1;
					returnCode = 10;
					retValue = "미안.. 구현중...";
					/////////////////////////////////////////////////////////
				}
				else if (length == defaultLength + 1){
					//[POS] [NAME] (STADIUM) [RECORD]
					queryParam.put("POS", inputStr[0]);
					queryParam.put("NAME", inputStr[1]);
					queryParam.put("STADIUM", inputStr[2]);
					queryParam.put("RECORD", inputStr[3]);
					/////////////////////////////////////////////////////////
					caseNum = 2;
					returnCode = 11;
					retValue = "미안.. 구현중...";
					/////////////////////////////////////////////////////////
				}
				else {
					caseNum = 0;
					returnCode = 12;
					retValue = "혹시? [Pos] [N] (S) [R]";
				}
					
				if (caseNum > 0) {
					String selectStr = recordBO.getRecord(queryParam);
					retValue = String.format("%s %s : [%s = %s]", 
									queryParam.get("POS"), queryParam.get("NAME"), queryParam.get("RECORD"), selectStr);
				}
				break;
					
			case 1:
				//(WHERE) [POS] [NAME] (STADIUM) [RECORD]
				if (inputStr[0].split(":").length != 2) 
				{
					//(WHERE) [POS] [NAME] [RECORD]
					if (length == defaultLength + cri) {
						queryParam.put("WHERE", inputStr[0]);
						queryParam.put("POS", inputStr[1]);
						queryParam.put("NAME", inputStr[2]);
						queryParam.put("RECORD", inputStr[3]);
						/////////////////////////////////////////////////////////
						caseNum = 3;
						returnCode = 20;
						retValue = "미안.. 구현중...";
						/////////////////////////////////////////////////////////
					}
					else if (length == defaultLength + cri + 1){
						//(WHERE) [POS] [NAME] (STADIUM) [RECORD]
						queryParam.put("WHERE", inputStr[0]);
						queryParam.put("POS", inputStr[1]);
						queryParam.put("NAME", inputStr[2]);
						queryParam.put("STADIUM", inputStr[3]);
						queryParam.put("RECORD", inputStr[4]);
						/////////////////////////////////////////////////////////
						caseNum = 4;
						returnCode = 21;
						retValue = "미안.. 구현중...";
						/////////////////////////////////////////////////////////
					}
					else {
						caseNum = 0;
						returnCode = 22;
						retValue = "혹시? (W) [Pos] [N] (S) [R]";
					}
				}
				else if (inputStr[0].split(":").length == 2){
					if (length == defaultLength + cri) {
						//([PERIOD]:[GAME_NUM]) [POS] [NAME] [RECORD].
						queryParam.put("PERIOD", inputStr[0].split(":")[0]);
						queryParam.put("GAME_NUM", inputStr[0].split(":")[1]);
						queryParam.put("POS", inputStr[1]);
						queryParam.put("NAME", inputStr[2]);
						queryParam.put("RECORD", inputStr[3]);
						/////////////////////////////////////////////////////////
						caseNum = 5;
						returnCode = 30;
						retValue = "미안.. 구현중...";
						/////////////////////////////////////////////////////////
					}
					else if (length == defaultLength + cri + 1){
						//([PERIOD]:[GAME_NUM]) [POS] [NAME] (STADIUM) [RECORD]
						queryParam.put("PERIOD", inputStr[0].split(":")[0]);
						queryParam.put("GAME_NUM", inputStr[0].split(":")[1]);
						queryParam.put("POS", inputStr[1]);
						queryParam.put("NAME", inputStr[2]);
						queryParam.put("STADIUM", inputStr[3]);
						queryParam.put("RECORD", inputStr[4]);
						/////////////////////////////////////////////////////////
						caseNum = 6;
						returnCode = 31;
						retValue = "미안.. 구현중...";
						/////////////////////////////////////////////////////////
					}
					else {
						caseNum = 0;
						returnCode = 32;
						retValue = "혹시? (P:G) [Pos] [N] (S) [R]";
					}
				}
				else {
					caseNum = 0;
					returnCode = 660;
					retValue = "에러! [기간.게임수]를 입력..";
				}
				break;
					
			case 2:
				//(WHERE) ([PERIOD]:[GAME_NUM]) [POS] [NAME] (STADIUM) [RECORD]
				if (inputStr[1].split(":").length != 2){
					caseNum = 0;
					returnCode = 661;
					retValue = "에러! [기간.게임수]를 입력..";
					break;
				}
			
				if (length == defaultLength + cri) {
					//(WHERE) ([PERIOD]:[GAME_NUM]) [POS] [NAME] [RECORD]
					queryParam.put("WHERE", inputStr[0]);
					queryParam.put("PERIOD", inputStr[1].split(":")[0]);
					queryParam.put("GAME_NUM", inputStr[1].split(":")[1]);
					queryParam.put("POS", inputStr[2]);
					queryParam.put("NAME", inputStr[3]);
					queryParam.put("RECORD", inputStr[4]);
					/////////////////////////////////////////////////////////
					caseNum = 7;
					returnCode = 41;
					retValue = "미안.. 구현중...";
					/////////////////////////////////////////////////////////
				}
				else if (length == defaultLength + cri + 1){
					//(WHERE) ([PERIOD]:[GAME_NUM]) [POS] [NAME] (STADIUM) [RECORD]
					queryParam.put("WHERE", inputStr[0]);
					queryParam.put("PERIOD", inputStr[1].split(":")[0]);
					queryParam.put("GAME_NUM", inputStr[1].split(":")[1]);
					queryParam.put("POS", inputStr[2]);
					queryParam.put("NAME", inputStr[3]);
					queryParam.put("STADIUM", inputStr[4]);
					queryParam.put("RECORD", inputStr[5]);
					/////////////////////////////////////////////////////////
					caseNum = 8;
					returnCode = 42;
					retValue = "미안.. 구현중...";
					/////////////////////////////////////////////////////////
				}
				else {
					caseNum = 0;
					returnCode = 43;
					retValue = "혹시? (W) (P:G) [Pos] [N] (S) [R]";
				}
				break;
			}
		} 
		catch (Exception e) {
			caseNum = 0;
			returnCode = 998;
			retValue = "미안.. 확인되지 않은 오류!";
		}
		
		resultMap.put("case", caseNum);
		resultMap.put("code", returnCode);
		resultMap.put("retValue", retValue);
		
		return resultMap;
	}
}
