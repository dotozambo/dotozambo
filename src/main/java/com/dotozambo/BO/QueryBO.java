package com.dotozambo.BO;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.stereotype.Component;

@Component
public class QueryBO
{
	public int inputStr2QueryStr(String inputstring) throws UnsupportedEncodingException 
	{
		//(WHERE). ([Period]. [GAME_NUM].). [POS]. [NAME]. [RECORD]. (STADIUM).

		String [] inputStr = inputstring.split(" ");
		
		int length = inputStr.length;
		int cri = -1;
		
		if (length < 3) {
			//Input Fail (Minimum 3)
			return -1;
		}
		
		for (int i = 0; i < length; i++) {
			if (URLEncoder.encode(inputStr[i], "utf-8").equals("%ED%88%AC%EC%88%98") /*투수*/
					|| URLEncoder.encode(inputStr[i], "utf-8").equals("%ED%83%80%EC%9E%90") /*타자*/) {
				cri = i;
				break;
			}
		}
		
		int defaultLength = 3;
		int caseNum = -1;
		switch (cri) {
			case -1:
				//Input Fail
				break;
			case 0:
				//[POS]. [NAME]. (STADIUM). [RECORD].
				if (length == defaultLength) {
					//[POS]. [NAME]. [RECORD].
					caseNum = 0;
				}
				else {
					//[POS]. [NAME]. (STADIUM). [RECORD].
					caseNum = 1;
				}
				break;
		case 1:
			//(WHERE). [POS]. [NAME]. (STADIUM). [RECORD].
			if (length == defaultLength + cri) {
				//(WHERE). [POS]. [NAME]. [RECORD].
				caseNum = 2;
			}
			else {
				//(WHERE). [POS]. [NAME]. (STADIUM). [RECORD].
				caseNum = 3;
			}
			break;
		case 2:
			//([Period]. [GAME_NUM].) [POS]. [NAME]. (STADIUM). [RECORD].
			if (length == defaultLength + cri) {
				//([Period]. [GAME_NUM].) [POS]. [NAME]. [RECORD].
				caseNum = 4;
			}
			else {
				//([Period]. [GAME_NUM].) [POS]. [NAME]. (STADIUM). [RECORD].
				caseNum = 5;
			}
			break;
		case 3:
			//(WHERE). ([Period]. [GAME_NUM].) [POS]. [NAME]. (STADIUM). [RECORD].
			if (length == defaultLength + cri) {
				//(WHERE). ([Period]. [GAME_NUM].) [POS]. [NAME]. [RECORD].
				caseNum = 6;
			}
			else {
				//(WHERE). ([Period]. [GAME_NUM].) [POS]. [NAME]. (STADIUM). [RECORD].
				caseNum = 7;
			}
		default:
			break;
		}
		
		if (caseNum == -1){
			//Input Fail
			return -1;
		}
		
		return caseNum;
	}
}
