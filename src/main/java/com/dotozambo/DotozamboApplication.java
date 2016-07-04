package com.dotozambo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.client.DefaultLineBotClient;
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
@ComponentScan("com.linecorp")
@Slf4j
public class DotozamboApplication {
	
	//Chat Member 
	static List<String> memberMIDsArry;
	
	static String notSupportMsg = "[Dotozambo] : Sorry.., It does Not Supprot Messages!";
	static String welcomeMsg = "[Dotozambo] : Welcome to My friend!";
	
	@RequestMapping("/heart_beat")
	public String healthCheck() {
		return "Still Alive..";
	}
	
	@RequestMapping("/")
    public String index(){
		return "Welcome to Dotozambo";
    }
	
	public static void main(String[] args) {
		
		memberMIDsArry = new ArrayList<String>();
		SpringApplication.run(DotozamboApplication.class, args);
	}
	
	@RestController
    public static class MyController {
		
		@Autowired
        private LineBotClient lineBotClient;
		
		@RequestMapping("/line_bot_callback")
        public void callback(@LineBotMessages List<Event> events) throws LineBotAPIException 
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
            		if (abOperation instanceof AddedAsFriendOperation) {
            			//Added Friend
            			lineBotClient.sendText(abOperation.getMid(), welcomeMsg);
            			
            			memberMIDsArry.add(abOperation.getMid());
            			memberMIDsArry = new ArrayList<String>(new HashSet<String>(memberMIDsArry));
            			
            		}
            		if (abOperation instanceof BlockedOperation) {
            			//Blocked Frend
            			String userMID = abOperation.getMid();
            			memberMIDsArry.remove(userMID);
            			memberMIDsArry = new ArrayList<String>(new HashSet<String>(memberMIDsArry));
            		}
            	}
            }
        }
		
		@RequestMapping("/line_bot_sendMsg")
		public String toLinebotSendMessage(@RequestParam("msg") String sendMsg) throws LineBotAPIException 
		{
			String noticeMsg = "Notice : " + sendMsg;
			String returnStr = "";
			
			for (int i = 0; i < memberMIDsArry.size(); i++)
			{
				lineBotClient.sendText(memberMIDsArry.get(i), noticeMsg);
				returnStr = returnStr + memberMIDsArry.get(i);
				returnStr = returnStr + "<br>";
			}
			
			return returnStr;
		}
	}
}
