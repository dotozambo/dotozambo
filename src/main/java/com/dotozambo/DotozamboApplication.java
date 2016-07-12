package com.dotozambo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dotozambo.DAO.ChatMembersDAO;
import com.dotozambo.DAO.ScoreBoardDAO;
import com.dotozambo.Model.ScoreBoard;
import com.linecorp.bot.client.LineBotClient;
import com.linecorp.bot.client.exception.LineBotAPIException;
import com.linecorp.bot.model.callback.Event;
import com.linecorp.bot.model.content.AbstractContent;
import com.linecorp.bot.model.content.AbstractOperation;
import com.linecorp.bot.model.content.AddedAsFriendOperation;
import com.linecorp.bot.model.content.BlockedOperation;
import com.linecorp.bot.model.content.TextContent;
import com.linecorp.bot.spring.boot.annotation.LineBotMessages;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@RestController
@ComponentScan("com")
@Slf4j
public class DotozamboApplication {
	
	static String notSupportMsg = "[Dotozambo] : Sorry.., It does Not Supprot Messages!";
	
	static int inningSize = 12;
	
	@RequestMapping("/heart_beat")
	public String healthCheck() {
		return "Still Alive..";
	}
	
	@RequestMapping("/")
    public String index(){
		return "Welcome to Dotozambo";
    }
	
	public static void main(String[] args) {
		SpringApplication.run(DotozamboApplication.class, args);
	}
	
	@RestController
    public static class MyController {
		
		@Autowired
        private LineBotClient lineBotClient;
		
		@Autowired
		ChatMembersDAO chatMemberDAO;
		@Autowired
		ScoreBoardDAO scoreBoardDAO;
		
		@RequestMapping("/line_bot_callback")
        public void callback(@LineBotMessages List<Event> events) throws LineBotAPIException, UnsupportedEncodingException 
		{
            for (Event event : events) {
            	
            	AbstractContent abContent = null;
            	AbstractOperation abOperation = null;
            	
            	try {
            		abContent = (AbstractContent) event.getContent();
            	}catch (Exception e) {
            		abOperation = (AbstractOperation) event.getContent();
            	}
            	
            	//Content
            	if (abContent != null)
            	{
            		//Text Messages
            		if (abContent instanceof TextContent) {
            			TextContent textContent = (TextContent) abContent;
            			lineBotClient.sendText(textContent.getFrom(), "[Dotozambo] : " + textContent.getText());
            		}
            		else {
            			lineBotClient.sendText(abContent.getFrom(), notSupportMsg);
            		}
            	}
            	//Operation
            	else if (abOperation != null) {
            		
            		String userMID = null;
            		
            		if (abOperation instanceof AddedAsFriendOperation) {
            			//Added Friend
            			userMID = abOperation.getMid();
            
            			List<String> mids = new ArrayList<String>();
            			mids.add(userMID);
            
            			String name = lineBotClient.getUserProfile(mids).getContacts().get(0).getDisplayName();
            			String welcomeMsg = new String (String.format("[Dotozambo] : Welcome to My friend %s", name));
            			
            			lineBotClient.sendText(userMID, welcomeMsg);
            			chatMemberDAO.addMember(userMID, name);
            	
            		}
            		if (abOperation instanceof BlockedOperation) {
            			//Blocked Frend
            			userMID = abOperation.getMid();
            			chatMemberDAO.deleteMember(userMID);
            		}
            	}
            }
        }
		
		@RequestMapping("/line_bot_send_notice")
		public String toLinebotSendMessage(
				@RequestParam("msg") String msg,
				HttpServletRequest request) throws LineBotAPIException, UnsupportedEncodingException 
		{
			String sendMsg = URLDecoder.decode(msg, "utf-8");
			sendMsg = sendMsg.replaceAll("%5Cn", "%0A");
			String noticeMsg = "Notice : " + sendMsg;
			
			List<Map <String, String>> members = chatMemberDAO.selectMember();
			String toUser = "";
			
			for (Map<String, String> member : members)
			{
				String mid = new String(String.valueOf(member.get("mid"))).trim();
				String name = new String(String.valueOf(member.get("name"))).trim();
				
				toUser = toUser + "<br> mid - " + mid + " / " + "name - " + name;
				lineBotClient.sendText(mid, noticeMsg);
			}
			
			noticeMsg = noticeMsg + toUser;
			return noticeMsg;
		}
		
		@RequestMapping("/kboschedule")
		public String kobSchedule(@RequestParam("month") int month, @RequestParam("year") int year) throws IOException 
		{
			String naverSportsKbaseballUrl = new String(String.format("http://sports.news.naver.com/kbaseball/schedule/index.nhn?month=%2d&year=%4d", month, year));
			String res = sendGet(naverSportsKbaseballUrl);
			
			Document doc = Jsoup.parse(res);
			Element totalDiv = doc.getElementsByClass("tb_wrap").get(0);
			Elements divList = totalDiv.getElementsByTag("div");
			
			String ret = "";
			for (int i = 1; i < divList.size(); i++) 
			{
				Element div = divList.get(i);
				Elements date = div.getElementsByClass("td_date");
			
				if (div.text().isEmpty() || date.text().isEmpty()) continue; //Not yet Playing Game
				
				String versus = "";
				for (int j = 0; j < 5; j++) 
				{
					Elements away_team = div.getElementsByClass("team_lft");
					Elements home_team = div.getElementsByClass("team_rgt");
					
					if (away_team.text().isEmpty() || home_team.text().isEmpty()) break; //No Game
					
					versus = versus + "<br>" + 
							away_team.get(j).text() + ": [A] vs [H] : " + home_team.get(j).text() + "<br>";
				}
				
				ret = ret + "<br>" + date.text() + "<br>" + versus;
			}
			
			return ret;
		}
		
		@RequestMapping("/saveRecord")
		public String saveRecord(@RequestParam("date") String date,
								 @RequestParam("homeTCode") String homeTCode,
								 @RequestParam("awayTCode") String awayTCode) throws IOException 
		{
			//Get Scorebox URL (Date / Home Team Code / Away Team Code)
			//http://www.koreabaseball.com/Schedule/Game/BoxScore.aspx?leagueId=1&seriesId=0&gameId=20160401HHLG0&gyear=2016
			
			String year = date.substring(0, 4);
			String month = date.substring(4, 6);
			String day = date.substring(6, 8);
			
			String scoreBoxUrl = "http://www.koreabaseball.com/Schedule/Game/BoxScore.aspx";
			String requestUrl = new String(String.format(
					"%s?leagueId=1&seriesId=0&gameId=%s%s%s%s%s0&gyear=%s", 
					scoreBoxUrl, year, month, day, awayTCode, homeTCode, year));
			
			String res = sendGet(requestUrl);
			Document doc = Jsoup.parse(res);
			
			//Get Scorebox values
			//ScoreBox
			//[date], away_team, home_team, away_score, home_score, away_r, away_h, away_e, away_b, home_r, home_h, home_e, home_b
			Element socreBoardTable = doc.getElementsByClass("socreBoard").get(0);
			socreBoardTable = socreBoardTable.getElementsByTag("tbody").get(0);
			Element awayScoreTr = socreBoardTable.getElementsByTag("tr").get(0);
			Map <String, Object> awayScoreMap = getScoreBoard(awayScoreTr, "away");
			
			Element homeScoreTr = socreBoardTable.getElementsByTag("tr").get(1);
			Map <String, Object> homeScoreMap = getScoreBoard(homeScoreTr, "home");
			
			Map <String, Object> scoreMap = new HashMap<String, Object>();
			scoreMap.put("away", awayScoreMap);
			scoreMap.put("home", homeScoreMap);
			
			ScoreBoard sb = new ScoreBoard(scoreMap, "20160401");
			scoreBoardDAO.addScoreBoard(sb);
			
			//Hitter
			//[date], team_code, [name], stadium, [order], num, [position], record, ab, h, rbi, r, [avg]
			Element boxscoreDiv = doc.getElementsByClass("boxscore").get(0);
			Element awayHitterTable = boxscoreDiv.getElementsByClass("tData").get(0);
			awayHitterTable = awayHitterTable.getElementsByTag("tbody").get(0);
			Elements awayHitterTr = awayHitterTable.getElementsByTag("tr");
			String awayHitterResult = getHitterResult(awayHitterTr, inningSize);		
			
			Element homeHitterTable = boxscoreDiv.getElementsByClass("tData").get(1);
			homeHitterTable = homeHitterTable.getElementsByTag("tbody").get(0);
			Elements homeHitterTr = homeHitterTable.getElementsByTag("tr");
			String homeHitterResult = getHitterResult(homeHitterTr, inningSize);	
			
			//Pitcher
			//[date], stadium, num, [name], si, result, w, l,	sv, ip, tbf, np, pa, h, hr, hbp, so, r, er, era
			Element awayPitcherTable = boxscoreDiv.getElementsByClass("tData").get(2);
			awayPitcherTable = awayPitcherTable.getElementsByTag("tbody").get(0);
			Elements awayPitcherTr = awayPitcherTable.getElementsByTag("tr");
			String awayPitcherResult = getPitcherResult(awayPitcherTr);
			
			Element homePitcherTable = boxscoreDiv.getElementsByClass("tData").get(3);
			homePitcherTable = homePitcherTable.getElementsByTag("tbody").get(0);
			Elements homePitcherTr = homePitcherTable.getElementsByTag("tr");
			String homePitcherResult = getPitcherResult(homePitcherTr);
			
			//Insert Scorebox DB
			//Insert Home Hitter Record
			//Insert Away Hitter Record
			//Insert Home Pitcher Record
			//Insert Away Picher Record
			
			String result = "<br><br>" + awayHitterResult.replaceAll("\n", "<br>") + "<br>" + homeHitterResult.replaceAll("\n", "<br>") + "<br>"
					+ "<br><br>" + awayPitcherResult.replaceAll("\n", "<br>") + "<br>" + homePitcherResult.replaceAll("\n", "<br>") + "<br>";
			
			return result;
		}
		
		@RequestMapping("/getTodayGames")
		public ModelAndView getTodayGames(RedirectAttributes redirectAttributes) throws IOException 
		{
			//Date today = new Date();
			//SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
			//String dateStr = dateFormater.format(today);
			
			String naverUrl = "http://sports.news.naver.com/kbaseball/index.nhn";
			String res = sendGet(naverUrl);
			Document doc = Jsoup.parse(res);
			
			Element scheduleBox = doc.getElementById("_schedule_box");
			String date = scheduleBox.getElementsByClass("day").get(0).text();
			
			Elements games = scheduleBox.getElementsByClass("hmb_list_items");
			
			String table = "\n***************\n[" + date + "]\n***************\n";
			for (Element _div : games) 
			{
				Elements detail = _div.getElementsByClass("inner");
				
				Element awayDiv = detail.get(0);
				String away_team = awayDiv.text().split(" ")[0];
				String away_starter = awayDiv.text().split(" ")[1];
				
				Element homeDiv = detail.get(1);
				String home_team = homeDiv.text().split(" ")[0];
				String home_starter = homeDiv.text().split(" ")[1];
				
				Element timeDiv = detail.get(2);
				
				String line = new String(String.format("%s (%s)\n[%s]\n%s (%s)\n***************\n",
								away_team, away_starter, timeDiv.text(), home_team, home_starter));
				table = table + line;
			}
			
			table = URLEncoder.encode(table, "utf-8");
			
			return new ModelAndView("redirect:/line_bot_send_notice?msg=" + table);
		}
	}
	private static Map<String, Object> getScoreBoard(Element scoreelement, String stadium)
	{
		Map <String, Object> resultMap = new HashMap<String, Object>();
		
		//[한화, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 13, 2, 3]
		//[LG, 0, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 5, 9, 0, 4]
		String [] scoreArray = scoreelement.text().split(" ");
		ArrayList<String> scoreList = new ArrayList<String>(Arrays.asList(scoreArray));
		
		resultMap.put(stadium + "_team", scoreList.get(0));
		scoreList.remove(0);
		resultMap.put(stadium + "_r", scoreList.get(inningSize));
		scoreList.remove(inningSize);
		resultMap.put(stadium + "_h", scoreList.get(inningSize));
		scoreList.remove(inningSize);
		resultMap.put(stadium + "_e", scoreList.get(inningSize));
		scoreList.remove(inningSize);
		resultMap.put(stadium + "_b", scoreList.get(inningSize));
		scoreList.remove(inningSize);
		resultMap.put(stadium + "_score", scoreList);
		
		return resultMap;
	}
	
	private static String getHitterResult(Elements hitterelements, int inningSize) 
	{
		String result = "";
		int preOrder = 0;
		int num = 0;
		for (Element hitter : hitterelements)
		{
			String person = "";
			String order = hitter.getElementsByTag("th").get(0).text();
			
			String postion = hitter.getElementsByTag("th").get(1).text();
			String name = hitter.getElementsByTag("th").get(2).text();
			
			List<String> record = new ArrayList<String>(inningSize);
			for (int i = 0; i < inningSize - 4; i++) {
				record.add(hitter.getElementsByTag("td").get(i).text());
			}
			
			String ab = hitter.getElementsByAttribute("abbr").get(0).text();
			String h = hitter.getElementsByAttribute("abbr").get(1).text();
			String rbi = hitter.getElementsByAttribute("abbr").get(2).text();
			String r = hitter.getElementsByAttribute("abbr").get(3).text();
			String avg = hitter.getElementsByAttribute("abbr").get(4).text();
			
			int curOrder = Integer.parseInt(order);
			if (preOrder == curOrder) num++;
			else num = 0; preOrder = curOrder;
			
			person = new String(String.format("%s, %d, %s, %s, [%s], %s, %s, %s, %s, %s\n", 
												order, num, postion, name, record.toString(), ab, h, rbi, r, avg));
			
			result = result + person;
		}
		
		return result;
	}
	
	//Pitcher
	//[date], stadium, num, [name], si, result, w, l,	sv, ip, tbf, np, pa, h, hr, hbp, so, r, er, era
	private static String getPitcherResult(Elements pitcherelements)
	{
		String result = "";
		for (Element pitcher : pitcherelements)
		{
			String person = "";
			String name = pitcher.getElementsByClass("name").get(0).text();
			String si = pitcher.getElementsByTag("td").get(0).text();
			String _result = pitcher.getElementsByTag("td").get(1).text();
			String w = pitcher.getElementsByTag("td").get(2).text();
			String l = pitcher.getElementsByTag("td").get(3).text();
			String sv = pitcher.getElementsByTag("td").get(4).text();
			String ip = pitcher.getElementsByTag("td").get(5).text();
			String tbf = pitcher.getElementsByTag("td").get(6).text();
			String np = pitcher.getElementsByTag("td").get(7).text();
			String pa = pitcher.getElementsByTag("td").get(8).text();
			String h = pitcher.getElementsByTag("td").get(9).text();
			String hr = pitcher.getElementsByTag("td").get(10).text();
			String hbp = pitcher.getElementsByTag("td").get(11).text();
			String so = pitcher.getElementsByTag("td").get(12).text();
			String r = pitcher.getElementsByTag("td").get(13).text();
			String er = pitcher.getElementsByTag("td").get(14).text();
			String era = pitcher.getElementsByTag("td").get(15).text();
			
			person = new String(String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s\n", 
												name, si, _result, w, l, sv, ip, tbf, np, pa, h, hr, hbp, so, r, er,era));
			
			result = result + person;
		}
		
		return result;
	}
	
	private static String sendGet(String getUrl) throws IOException 
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