package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TeamCodeDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private URLCodec urlCodec;
	
	public Map<String, String> selectTeamCode() throws UnsupportedEncodingException 
	{
		String sql = "SELECT team_code, rankname FROM kboteams";
		
		List <Map<String, Object>> teams = jdbcTemplate.queryForList(sql);
		Map<String, String> encodedMap = new HashMap<String, String> ();
		for (Map<String, Object> team : teams) 
		{
			encodedMap.put(urlCodec.urlDecoded(String.valueOf(team.get("rankname"))).toLowerCase(), 
							urlCodec.urlDecoded(String.valueOf(team.get("team_code"))));
		}
		return encodedMap;
	}

}
