package com.dotozambo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.dotozambo.DAO.ChatMembersDAO;
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
		public String toLinebotSendMessage(@RequestParam("msg") String sendMsg) throws LineBotAPIException, UnsupportedEncodingException 
		{
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
		
		@RequestMapping("/getTodayGames")
		public ModelAndView getTodayGames() throws IOException 
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
			
			String table = date + "<br>";
			for (Element _div : games) 
			{
				String list = "";
				Elements detail = _div.getElementsByTag("span");
				String away_team = detail.get(0).text();
				String away_sp = detail.get(1).text();
				String home_team = detail.get(2).text();
				String home_sp = detail.get(3).text();
				
				list = "("+ away_sp +")" + away_team + ": Away VS Home : "  + home_team + "("+ home_sp + ")<br>";
				table = table + list;
			}
			
			return new ModelAndView("redirect:/line_bot_send_notice?msg=" + table);
		}
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