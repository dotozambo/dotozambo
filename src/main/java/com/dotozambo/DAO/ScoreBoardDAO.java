package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dotozambo.Model.ScoreBoard;

@Component
public class ScoreBoardDAO 
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
 
    public int addScoreBoard(ScoreBoard sb) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO scoreboard([date], away_team, home_team, away_score, home_score, away_r, away_h, away_e, away_b, home_r, home_h, home_e, home_b) "
    								+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	Object[] args = {sb.getDate(), 
    					 sb.getAway_team(), sb.getHome_team(), 
    					 sb.getAway_score(), sb.getHome_score(), 
    					 sb.getAway_r(), sb.getAway_h(), sb.getAway_e(), sb.getAway_b(),
    					 sb.getHome_r(), sb.getHome_h(), sb.getHome_e(), sb.getHome_b()};
    	
    	return jdbcTemplate.update(sql,args);
    }
    
    public String selectLatestGameDate() 
    {
    	String sql = "SELECT [date] FROM scoreboard ORDER BY [date] DESC LIMIT 1";
    	String latestGameDate = jdbcTemplate.queryForObject(sql, null, String.class);
    	return latestGameDate;
    }
}
