package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dotozambo.BO.URLCodecBO;

@Component
public class ChatMembersDAO {
 
	@Autowired
	private JdbcTemplate jdbcTemplate;
 
	@Autowired
	private URLCodecBO urlCodecBO;
	
    public int addMember(String mid, String name) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO chatmembers(mid, name) values(?, ?)";
    	Object[] args = {urlCodecBO.encode(mid), urlCodecBO.encode(name)};
    	return jdbcTemplate.update(sql,args);
    }

	public int deleteMember(String mid) throws UnsupportedEncodingException 
	{
		String sql = "DELETE FROM chatmembers WHERE mid = ?";
		Object[] args = {urlCodecBO.encode(mid)};
		return jdbcTemplate.update(sql,args);
	}
	
	public List<Map<String, String>> selectMember() throws UnsupportedEncodingException 
	{
		String sql = "SELECT mid, name FROM chatmembers";
		List <Map<String, Object>> members = jdbcTemplate.queryForList(sql);
		
		List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		for (Map<String, Object> member : members) 
		{
			Map<String, String> encodedMap = new HashMap<String, String> ();
			
			encodedMap.put("mid", urlCodecBO.encode(String.valueOf(member.get("mid"))));
			encodedMap.put("name", urlCodecBO.decode(String.valueOf(member.get("name"))));
			retList.add(encodedMap);
		}
		return retList;
	}
}