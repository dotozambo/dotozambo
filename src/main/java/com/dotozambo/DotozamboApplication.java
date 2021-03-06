package com.dotozambo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import com.dotozambo.BO.CommonBO;
import com.dotozambo.BO.RecordBO;
import com.dotozambo.BO.TodayGamesBO;
import com.dotozambo.DAO.ChatMembersDAO;
import com.dotozambo.DAO.HitterRecordDAO;
import com.dotozambo.DAO.PitcherRecordDAO;
import com.dotozambo.DAO.ScoreBoardDAO;
import com.dotozambo.DAO.TeamCodeDAO;
import com.dotozambo.Model.HitterRecord;
import com.dotozambo.Model.PitcherRecord;
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

@SpringBootApplication
@ComponentScan("com")
public class DotozamboApplication {
	
	static String notSupportMsg = "지원하지 않는 메시지입니다..";
	static String incorrectFormatMsg = "(조건) ([기간]:[게임수]) [투수/타자] [이름] (홈/어웨이) [기록]";
	
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
		ChartController chartController;
		@Autowired
		QueryController queryController;
		
		
		@Autowired
		CommonBO commonBO;
		@Autowired
		TodayGamesBO todayGamesBO;
		@Autowired
		RecordBO recordBO;
		
		@Autowired
		ChatMembersDAO chatMemberDAO;
		@Autowired
		ScoreBoardDAO scoreBoardDAO;
		@Autowired
		HitterRecordDAO hitterRecordDAO;
		@Autowired
		PitcherRecordDAO pitcherRecordDAO;
		@Autowired
		TeamCodeDAO teamCodeDAO;
		
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
            			
            			String retMessage = getQueryRetunValue(textContent.getText());
            			lineBotClient.sendText(abContent.getFrom(), retMessage);
            		}
            		else {
            			lineBotClient.sendText(abContent.getFrom(), notSupportMsg);
            			return;
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
            			return;
            	
            		}
            		if (abOperation instanceof BlockedOperation) {
            			//Blocked Frend
            			userMID = abOperation.getMid();
            			chatMemberDAO.deleteMember(userMID);
            			return;
            		}
            	}
            }
        }
		
		@RequestMapping("/querytest")
		public String getQueryRetunValue(@RequestParam("query") String query) throws UnsupportedEncodingException
		{
			String msg = new String();
			Map <String, Object> result = queryController.inputStr2QueryStr(query);
			String resultStr = (String) result.get("retValue");
			int retCode = (int) result.get("code");
			int caseNum = (int) result.get("case");
			
			if (caseNum == 0) {
				msg = String.format("[%d] Query 절반성공.. - %s", retCode, resultStr);
			}
			else if (caseNum != -1) {
				msg = String.format("[%d] Query 성공! - %s", caseNum, resultStr);
			}
			else 
			{
				msg = String.format("[%d] Query 실패! - %s", retCode, resultStr);
			}
			return msg;
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
				String mid = new String(String.valueOf(member.get("mid")));
				String name = new String(String.valueOf(member.get("name")));
				
				toUser = toUser + "<br> mid - " + mid + " / " + "name - " + name;
				lineBotClient.sendText(mid, noticeMsg);
			}
			
			noticeMsg = noticeMsg + toUser;
			return noticeMsg;
		}
		
		@RequestMapping("/kboschedule")
		public Map<String, Object> kboSchedule(@RequestParam("month") int month, @RequestParam("year") int year) throws IOException 
		{
			String naverSportsKbaseballUrl = new String(String.format("http://sports.news.naver.com/kbaseball/schedule/index.nhn?month=%02d&year=%04d", month, year));
			String res = commonBO.sendGet(naverSportsKbaseballUrl);
			
			Document doc = Jsoup.parse(res);
			Element totalDiv = doc.getElementsByClass("tb_wrap").get(0);
			Elements tableList = totalDiv.getElementsByTag("table");
			
			Map <String, String> teamCodeMap = teamCodeDAO.selectTeamCode();
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			for (Element table : tableList) 
			{
				Map<String, Object> dateMap = new HashMap<String, Object>();
				
				Elements trList = table.getElementsByTag("tr");
				String date = commonBO.dateFormat(table.getElementsByClass("td_date").text(), year);
			
				if (trList.size() < 2) continue; //No Game Day
				else {
					dateMap.put("date", date);
				}
				
				for (Element tr : trList) 
				{
					Map <String, Object> versusMap = new HashMap<String, Object>();
					
					String score = "";
					if (tr.getElementsByTag("em").get(0).text().equals("VS")) {
						//Canceled Game
						continue;
					}
					else {
						score = tr.getElementsByClass("td_score").get(0).text();
					}
					
					String away_team = teamCodeMap.get(tr.getElementsByClass("team_lft").get(0).text().toLowerCase());
					String home_team = teamCodeMap.get(tr.getElementsByClass("team_rgt").get(0).text().toLowerCase());
					
					versusMap.put("away_team", away_team);
					versusMap.put("home_team", home_team);
					versusMap.put("score", score);
					
					dateMap.put(home_team, versusMap);
				}
				if (dateMap.keySet().size() == 1) continue; //All Canceled or Not Yet Play Games
				else resultMap.put(date, dateMap);
				
			}
			return resultMap;
		}
		@RequestMapping("/saveLatestGameDate")
		public String saveLatestGameDate() throws IOException
		{
			Date toDay = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			String toDayStr = sdf.format(toDay);
			
			int year = Integer.parseInt(toDayStr.split("-")[0]);
			int month = Integer.parseInt(toDayStr.split("-")[1]);
			
			Map<String, Object> gameResultMap = kboSchedule(month, year);
			List <String> gameList = new ArrayList<String>();
			gameList.addAll(gameResultMap.keySet());
			
			Collections.sort(gameList, new Comparator<String>() {
			      public int compare(String obj1, String obj2)
			      {
			    	  //Sort by DESC
			    	  return obj2.compareToIgnoreCase(obj1);
			      }
			});
			
			String latestGameDate = gameList.get(0);
			String savedLatestGameDate = scoreBoardDAO.selectLatestGameDate();
			//String latestGameDate = "20160811";
			//String savedLatestGameDate = "20160810";
			boolean isSaved = false;
			if (latestGameDate.compareTo(savedLatestGameDate) > 0) {
				//SaveRecord
				@SuppressWarnings("unchecked")
				Map <String, Object> games = (Map <String, Object>) gameResultMap.get(latestGameDate);
				for (String key : games.keySet()) {
					if (key.length() == 2) 
					{
						@SuppressWarnings("unchecked")
						Map <String, String> gameMap = (Map <String, String>) games.get(key);
						String homeTCode = gameMap.get("home_team");
						String awayTCode = gameMap.get("away_team");
						
						if (!saveRecord(latestGameDate, homeTCode, awayTCode).equals("OK"))
						{
							Map <String, String> errorMap = new HashMap<String, String>();
							errorMap.put("latestGameDate", latestGameDate);
							errorMap.put("home_team", homeTCode);
							errorMap.put("away_team", awayTCode);
							return errorMap.toString();
						}
					}
				}
				isSaved = true;
			}
			if (isSaved)
				return latestGameDate + "-Record_Saved_Complete";
			else
				return latestGameDate + "-Recording_Pass";
		}
		
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
			
			String res = commonBO.sendGet(requestUrl);
			Document doc = Jsoup.parse(res);
			
			//Get Scorebox values
			//ScoreBox
			//[date], away_team, home_team, away_score, home_score, away_r, away_h, away_e, away_b, home_r, home_h, home_e, home_b
			///////////////////////////////////////////////////////////////////////////
			Element socreBoardTable = doc.getElementsByClass("socreBoard").get(0);
			socreBoardTable = socreBoardTable.getElementsByTag("tbody").get(0);
			
			Element awayScoreTr = socreBoardTable.getElementsByTag("tr").get(0);
			Map <String, Object> awayScoreMap = getScoreBoard(awayScoreTr, awayTCode, "away");
			
			Element homeScoreTr = socreBoardTable.getElementsByTag("tr").get(1);
			Map <String, Object> homeScoreMap = getScoreBoard(homeScoreTr, homeTCode,"home");
			
			Map <String, Object> scoreMap = new HashMap<String, Object>();
			scoreMap.put("away", awayScoreMap);
			scoreMap.put("home", homeScoreMap);
			////////////////////////////////////////////////////////////////////////////
			//Hitter
			//[date], team_code, [name], stadium, [order], num, [position], record, ab, h, rbi, r, [avg]
			Element boxscoreDiv = doc.getElementsByClass("boxscore").get(0);
			Element awayHitterTable = boxscoreDiv.getElementsByClass("tData").get(0);
			awayHitterTable = awayHitterTable.getElementsByTag("tbody").get(0);
			Elements awayHitterTr = awayHitterTable.getElementsByTag("tr");
			List<Map<String, Object>> awayHitterResult = getHitterResult(awayHitterTr, inningSize);		
			////////////////////////////////////////////////////////////////////////////
			Element homeHitterTable = boxscoreDiv.getElementsByClass("tData").get(1);
			homeHitterTable = homeHitterTable.getElementsByTag("tbody").get(0);
			Elements homeHitterTr = homeHitterTable.getElementsByTag("tr");
			List<Map<String, Object>> homeHitterResult = getHitterResult(homeHitterTr, inningSize);	
			////////////////////////////////////////////////////////////////////////////
			//Pitcher
			//[date], stadium, num, [name], si, result, w, l,	sv, ip, tbf, np, pa, h, hr, hbp, so, r, er, era
			Element awayPitcherTable = boxscoreDiv.getElementsByClass("tData").get(2);
			awayPitcherTable = awayPitcherTable.getElementsByTag("tbody").get(0);
			Elements awayPitcherTr = awayPitcherTable.getElementsByTag("tr");
			List<Map<String, Object>> awayPitcherResult = getPitcherResult(awayPitcherTr);
			/////////////////////////////////////////////////////////////////////////////
			Element homePitcherTable = boxscoreDiv.getElementsByClass("tData").get(3);
			homePitcherTable = homePitcherTable.getElementsByTag("tbody").get(0);
			Elements homePitcherTr = homePitcherTable.getElementsByTag("tr");
			List<Map<String, Object>> homePitcherResult = getPitcherResult(homePitcherTr);
			//////////////////////////////////////////////////////////////////////////////
			ScoreBoard sb = new ScoreBoard(scoreMap, date);
			scoreBoardDAO.addScoreBoard(sb);
			for (Map<String, Object> hitter : awayHitterResult)	{
				HitterRecord away_hr = new HitterRecord(hitter, date, awayTCode, "away");
				hitterRecordDAO.addHitterRecord(away_hr);
			}
			for (Map<String, Object> hitter : homeHitterResult)	{
				HitterRecord home_hr = new HitterRecord(hitter, date, homeTCode, "home");
				hitterRecordDAO.addHitterRecord(home_hr);
			}
			for (Map<String, Object> pitcher : awayPitcherResult) {
				PitcherRecord away_pr = new PitcherRecord(pitcher, date, awayTCode, "away");
				pitcherRecordDAO.addPitcherRecord(away_pr);
			}
			for (Map<String, Object> pitcher : homePitcherResult) {
				PitcherRecord home_pr = new PitcherRecord(pitcher, date, homeTCode, "home");
				pitcherRecordDAO.addPitcherRecord(home_pr);
			}
			
			return "OK";
		}
		
		@SuppressWarnings("unchecked")
		@RequestMapping("/getlatestgameresult")
		public String getLatestGameResult(@RequestParam("game_num") int game_num) throws IOException, LineBotAPIException {
			
			Date today = new Date();
			SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
			String todayStr = dateFormater.format(today);
			
			List<Map <String, String>> gameMap = todayGamesBO.getTodayGamesMap(todayStr);
			
			/*
			List<Map <String, String>> gameMap = new ArrayList<Map <String, String>>();
			Map <String, String> test_1 = new HashMap<String, String>();
			test_1.put("away_team", "kt");
			test_1.put("home_team", "한화");
			gameMap.add(test_1);
			Map <String, String> test_2 = new HashMap<String, String>();
			test_2.put("away_team", "SK");
			test_2.put("home_team", "NC");
			gameMap.add(test_2);
			Map <String, String> test_3 = new HashMap<String, String>();
			test_3.put("away_team", "삼성");
			test_3.put("home_team", "두산");
			gameMap.add(test_3);
			*/
			
			Map <String, String> teamCode = teamCodeDAO.selectTeamCode();
			List<Map <String, Object>> rpRecordSet = new ArrayList<Map<String, Object>>();
			List<Map <String, Object>> scoreBoardSet = new ArrayList<Map<String, Object>>();
			
			Map <String, Object> away_team_score_map = new HashMap<String, Object>();
			Map <String, Object> home_team_score_map = new HashMap<String, Object>();
			
			for (Map <String, String> game : gameMap) 
			{	
				String away_team_code = teamCode.get(game.get("away_team").toLowerCase());
				away_team_score_map = scoreBoardDAO.selectLatestGameScoreBoard(away_team_code, game_num);
				
				String home_team_code = teamCode.get(game.get("home_team").toLowerCase());
				home_team_score_map = scoreBoardDAO.selectLatestGameScoreBoard(home_team_code, game_num);
				
				Map <String, Object> away_rpMap = recordBO.getRPitcherRecord_SUM(away_team_score_map, away_team_code);
				Map <String, Object> home_rpMap = recordBO.getRPitcherRecord_SUM(home_team_score_map, home_team_code);
				
				Map <String, Object> rpRecordMap = new HashMap<String, Object>();
				rpRecordMap.put(game.get("home_team"), home_rpMap);
				rpRecordMap.put(game.get("away_team"), away_rpMap);
				rpRecordSet.add(rpRecordMap);
				
				Map <String, Object> away_sbMap = recordBO.getScoreBoard_AVG(away_team_score_map, away_team_code);
				Map <String, Object> home_sbMap = recordBO.getScoreBoard_AVG(home_team_score_map, home_team_code);
				
				Map <String, Object> scoreRecordMap = new HashMap<String, Object>();
				scoreRecordMap.put(game.get("home_team"), home_sbMap);
				scoreRecordMap.put(game.get("away_team"), away_sbMap);
				scoreBoardSet.add(scoreRecordMap);
			}
			
			String rpRecordTotalStr = "";
			for (Map <String, Object> game : rpRecordSet) 
			{
				String resultGameStr = "";
				for (String team : game.keySet()) 
				{
					Map <String, Object> teamMap = (Map<String, Object>) game.get(team);
					int pitcher_num = 0;
					int npr_total = 0;
					double ip_total = 0.0;
					int er_total = 0;
					for (String pitcher : teamMap.keySet()) 
					{
						Map <String, Object> pitcherMap = (Map<String, Object>) teamMap.get(pitcher);
						
						/*
						String records = String.format("%s:[npr:%s ip:%s er:%s]\n", 
														pitcher, 
														pitcherMap.get("npr"),
														pitcherMap.get("ip"),
														pitcherMap.get("er"));
						 */
					
						pitcher_num++;
						npr_total += (int) pitcherMap.get("npr");
						ip_total += Double.parseDouble((String) pitcherMap.get("ip"));
						er_total += (int) pitcherMap.get("er");
					
					}
					String avg = String.format("총%d명:[총_npr:%d 게임평균_ip:%.1f 투수평균_er:%.1f]\n",
												pitcher_num, npr_total,((double) (ip_total / game_num)),((double) er_total / (pitcher_num)));
					
					resultGameStr = resultGameStr + team + " 최근 " + game_num + " 게임 [RP] 기록\n" + avg;
				}
				rpRecordTotalStr = rpRecordTotalStr + resultGameStr;
				//////////////////////////////////////////
				toLinebotSendMessage(resultGameStr, null);
				//log.info("RP Record {}", resultGameStr);
				//////////////////////////////////////////
			}
			
			String scoreboardRecordTotalStr = "";
			for (Map <String, Object> game : scoreBoardSet) 
			{
				String resultGameStr = "";
				for (String team : game.keySet()) 
				{
					Map <String, Object> homeSb = (Map<String, Object>) game.get(team);
				
					Map <String, Object> board = (Map<String, Object>) homeSb.get("score");
					List<Double> innings = (List<Double>) board.get("board");
					int r_total = (int) board.get("r_total");
					int h_total = (int) board.get("h_total");
					float r_avg = (float) board.get("r_avg");
					float h_avg = (float) board.get("h_avg");
				
					String scoreStr = String.format("이닝평균득점:[%s]\n게임평균득점:%.1f 게임평균안타:%.1f 총득점:%d 총안타:%d\n", 
													innings.toString(), r_avg, h_avg, r_total, h_total);
				
					board = (Map<String, Object>) homeSb.get("lost");
					innings = (List<Double>) board.get("board");
					r_total = (int) board.get("r_total");
					h_total = (int) board.get("h_total");
					r_avg = (float) board.get("r_avg");
					h_avg = (float) board.get("h_avg");
				
					String lostStr = String.format("이닝평균실점:[%s]\n게임평균실점:%.1f 게임평균피안타:%.1f 총실점:%d 총피안타:%d\n", 
													innings.toString(), r_avg, h_avg, r_total, h_total);
				
					resultGameStr = resultGameStr + team + " 최근 " + game_num + " 게임 [Score] 기록\n" + scoreStr + lostStr;
				}
				scoreboardRecordTotalStr = scoreboardRecordTotalStr + resultGameStr;
				///////////////////////////////////////////////////////////
				toLinebotSendMessage(resultGameStr, null);
				//log.info("ScoreBoard {}", resultGameStr);
				///////////////////////////////////////////////////////////
			}
			return (rpRecordTotalStr + scoreboardRecordTotalStr).replaceAll("\n", "<br>");
		}
		
		@RequestMapping("/getTodayGames")
		public ModelAndView getTodayGames(RedirectAttributes redirectAttributes) throws IOException 
		{
			Date today = new Date();
			SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
			String todayStr = dateFormater.format(today);
			
			List<Map <String, String>> gameList = todayGamesBO.getTodayGamesMap(todayStr);
			
			String table = "\n***************\n[" + todayStr + "]\n***************\n";
			if (gameList == null){
				String noGame = table + "오늘은 게임이 없습니다..";
				return new ModelAndView("redirect:/line_bot_send_notice?msg=" + noGame);
			}
			
			for (Map <String, String> gameMap : gameList) {
			
				String away_team = gameMap.get("away_team");
				String home_team = gameMap.get("home_team");
				String away_starter = gameMap.get("away_starter");
				String home_starter = gameMap.get("home_starter");
				String time = gameMap.get("time");
				
				Map <String, Object> away_spRecord = new HashMap<String, Object>();
				try {
					away_spRecord = pitcherRecordDAO.selectPitcherDefaultRecord(gameMap.get("away_starter"));
				}catch (Exception e) {
					away_spRecord.put("w", "?");
					away_spRecord.put("l", "?");
					away_spRecord.put("sv", "?");
					away_spRecord.put("era", "?");
				}
				Map <String, Object> home_spRecord = new HashMap<String, Object>();
				try {
					home_spRecord = pitcherRecordDAO.selectPitcherDefaultRecord(gameMap.get("home_starter"));
				} catch (Exception e) {
					home_spRecord.put("w", "?");
					home_spRecord.put("l", "?");
					home_spRecord.put("sv", "?");
					home_spRecord.put("era", "?");
			 	}
				String line = new String(String.format("%s\n%s(%sW/%sL/%sS/ERA:%s)\n[%s]\n%s\n%s(%sW/%sL/%sS/ERA:%s)\n***************\n",
									away_team, 
									away_starter, away_spRecord.get("w"), away_spRecord.get("l"), away_spRecord.get("sv"), away_spRecord.get("era"),
									time, 
									home_team, 
									home_starter, home_spRecord.get("w"), home_spRecord.get("l"), home_spRecord.get("sv"), home_spRecord.get("era")));
				
				table = table + line;
			}
			
			table = URLEncoder.encode(table, "utf-8");
			return new ModelAndView("redirect:/line_bot_send_notice?msg=" + table);
		}
	}
	
	private static Map<String, Object> getScoreBoard(Element scoreelement, String team_code, String stadium)
	{
		Map <String, Object> resultMap = new HashMap<String, Object>();
		
		//[한화, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 13, 2, 3]
		//[LG, 0, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 5, 9, 0, 4]
		String [] scoreArray = scoreelement.text().split(" ");
		ArrayList<String> scoreList = new ArrayList<String>(Arrays.asList(scoreArray));
		
		resultMap.put(stadium + "_team", team_code);
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
	
	private static List <Map <String, Object>> getHitterResult(Elements hitterelements, int inningSize) 
	{
		List <Map <String, Object>> hitterList = new ArrayList<Map <String, Object>>();
		int preOrder = 0;
		int num = 0;
		for (Element hitter : hitterelements)
		{
			Map<String, Object> person = new HashMap<String, Object>();
			person.put("order", hitter.getElementsByTag("th").get(0).text());
			person.put("position", hitter.getElementsByTag("th").get(1).text());
			person.put("name", hitter.getElementsByTag("th").get(2).text());
			
			List<String> recordArray = new ArrayList<String>(inningSize);
			for (int i = 0; i < inningSize - 4; i++) {
				recordArray.add(hitter.getElementsByTag("td").get(i).text());
			}
			person.put("record", recordArray.toString().substring(1, recordArray.toString().length() - 1));
			person.put("ab", hitter.getElementsByAttribute("abbr").get(0).text());
			person.put("h", hitter.getElementsByAttribute("abbr").get(1).text());
			person.put("rbi", hitter.getElementsByAttribute("abbr").get(2).text());
			person.put("r", hitter.getElementsByAttribute("abbr").get(3).text());
			person.put("avg", hitter.getElementsByAttribute("abbr").get(4).text());
			
			int curOrder = Integer.valueOf((String) person.get("order"));
			if (preOrder == curOrder) num++;
			else num = 0; preOrder = curOrder;
			person.put("num", num);
			
			hitterList.add(person);
		}
		
		return hitterList;
	}
	
	//Pitcher
	//[date], stadium, num, [name], si, result, w, l,	sv, ip, tbf, np, pa, h, hr, hbp, so, r, er, era
	private static List <Map <String, Object>> getPitcherResult(Elements pitcherelements)
	{
		List <Map <String, Object>> pitcherList = new ArrayList<Map <String, Object>>();
		for (Element pitcher : pitcherelements)
		{
			Map<String, Object> person = new HashMap<String, Object>();
			
			person.put("name", pitcher.getElementsByClass("name").get(0).text());
			person.put("si", pitcher.getElementsByTag("td").get(0).text());
			person.put("result", pitcher.getElementsByTag("td").get(1).text());
			person.put("w", pitcher.getElementsByTag("td").get(2).text());
			person.put("l", pitcher.getElementsByTag("td").get(3).text());
			person.put("sv", pitcher.getElementsByTag("td").get(4).text());
			person.put("ip", pitcher.getElementsByTag("td").get(5).text());
			person.put("tbf", pitcher.getElementsByTag("td").get(6).text());
			person.put("np", pitcher.getElementsByTag("td").get(7).text());
			person.put("pa", pitcher.getElementsByTag("td").get(8).text());
			person.put("h", pitcher.getElementsByTag("td").get(9).text());
			person.put("hr", pitcher.getElementsByTag("td").get(10).text());
			person.put("bb", pitcher.getElementsByTag("td").get(11).text());
			person.put("so", pitcher.getElementsByTag("td").get(12).text());
			person.put("r", pitcher.getElementsByTag("td").get(13).text());
			person.put("er", pitcher.getElementsByTag("td").get(14).text());
			person.put("era", pitcher.getElementsByTag("td").get(15).text());
			
			pitcherList.add(person);
		}
		
		return pitcherList;
	}
}