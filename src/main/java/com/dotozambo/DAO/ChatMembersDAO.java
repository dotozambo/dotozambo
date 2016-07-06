package com.dotozambo.DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMembersDAO {
 
	@Autowired
	private JdbcTemplate jdbcTemplate;
 
    public int addMember(String mid, String name) 
    {
    	String sql = "INSERT INTO chatmembers(mid, name) values(?, ?)";
    	Object[] args = {mid, name};
    	return jdbcTemplate.update(sql,args);
    }

	public int deleteMember(String mid) 
	{
		String sql = "DELETE FROM chatmembers WHERE mid = ?";
		Object[] args = {mid};
		return jdbcTemplate.update(sql,args);
	}
	
	public List <String> selectMember(String name) 
	{
		String sql = "SELECT mid FROM chatmembers";
		ArrayList <String> reultList = new ArrayList<>();
		List<Map<String, Object>> midMaps = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> midMap : midMaps){
			reultList.add(String.valueOf(midMap.get("mid")));
		}
		return reultList;
	}
}
