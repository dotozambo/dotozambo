package com.dotozambo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dotozambo.BO.RecordBO;
import com.dotozambo.BO.TodayGamesBO;
import com.dotozambo.DAO.ScoreBoardDAO;
import com.dotozambo.DAO.TeamCodeDAO;


import static com.googlecode.charts4j.Color.WHITE;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.ScatterPlot;
import com.googlecode.charts4j.ScatterPlotData;

@Controller
public class ChartController {
	
	@Autowired
	TodayGamesBO todayGamesBO;
	@Autowired
	RecordBO recordBO;
	
	@Autowired
	TeamCodeDAO teamCodeDAO;
	@Autowired
	ScoreBoardDAO scoreBoardDAO;
	
	@RequestMapping("/getlatestscoreboard")
	public String getLatestScoreboard(
			Model responseModel,
			@RequestParam("game_num") int game_num) throws IOException {
		
		Date today = new Date();
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
		String todayStr = dateFormater.format(today);
		
		List<Map <String, String>> gameMap = todayGamesBO.getTodayGamesMap(todayStr);
		
		Map <String, String> teamCode = teamCodeDAO.selectTeamCode();
		
		Map <String, Object> away_team_score_map = new HashMap<String, Object>();
		Map <String, Object> home_team_score_map = new HashMap<String, Object>();
		
		Map <String, Map <String, Object>> scoreBoardSet = new HashMap <String, Map<String, Object>>();
		
		for (Map <String, String> game : gameMap) 
		{	
			String away_team_code = teamCode.get(game.get("away_team").toLowerCase());
			away_team_score_map = scoreBoardDAO.selectLatestGameScoreBoard(away_team_code, game_num);
			
			String home_team_code = teamCode.get(game.get("home_team").toLowerCase());
			home_team_score_map = scoreBoardDAO.selectLatestGameScoreBoard(home_team_code, game_num);
			
			Map <String, Object> away_sbMap = recordBO.getScoreBoard_AVG(away_team_score_map, away_team_code);
			Map <String, Object> home_sbMap = recordBO.getScoreBoard_AVG(home_team_score_map, home_team_code);
			
			Map <String, Object> scoreRecordMap = new HashMap<String, Object>();
			scoreRecordMap.put(game.get("home_team"), home_sbMap);
			scoreRecordMap.put(game.get("away_team"), away_sbMap);
			scoreBoardSet.put(game.get("home_team"), scoreRecordMap);
		}
		
		Data d1 = Data.newData(10, 50, 30, 45, 65, 95, 20, 80);
        Data d2 = Data.newData(20, 40, 40, 15, 85, 95, 80, 20);
        
        ScatterPlotData data = Plots.newScatterPlotData(d1, d2);
        
        
        ScatterPlot chart = GCharts.newScatterPlot(data);
        chart.setSize(600, 450);
        chart.setGrid(20, 20, 3, 2);
        
        AxisLabels axisLabels = AxisLabelsFactory.newNumericRangeAxisLabels(0, 100);
        axisLabels.setAxisStyle(AxisStyle.newAxisStyle(WHITE, 13, AxisTextAlignment.CENTER));

        chart.addXAxisLabels(axisLabels);
        chart.addYAxisLabels(axisLabels);

        chart.setTitle("Scatter Plot", WHITE, 16);
		
		responseModel.addAttribute("chartUrl", chart.toURLString());
		return "scoreboardchart";
	}
	
}
