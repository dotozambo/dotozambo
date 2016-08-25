package com.dotozambo.BO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommonBO {
	
	public String dateFormat(String date, int year) 
	{
		try 
		{
			String _date = date.split(" ")[0].trim();
			DateFormat sdFormat = new SimpleDateFormat("yyyy M.dd");
			String tmpDateStr = new String(String.format("%04d ", year)) + _date;
			Date tmpDate = sdFormat.parse(tmpDateStr);
			
			DateFormat _sdFormat = new SimpleDateFormat("yyyyMMdd");
			return _sdFormat.format(tmpDate);
		} 
		catch (ParseException e) {
			e.printStackTrace();
			return date;
		}
	}
	
	public String sendGet(String getUrl) throws IOException 
	{
		URL obj = new URL(getUrl);
		String USER_AGENT = "Mozilla/5.0";
		
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestMethod("GET");
		
		int responseCode = con.getResponseCode();
		log.debug("\nSending 'GET' request to URL : {}" + getUrl);
		log.debug("Response Code : {}" + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		log.debug("Response : {}" + response.toString());
		return response.toString();
	}
	
}
