package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMembersDAO {
 
	@Autowired
	private JdbcTemplate jdbcTemplate;
 
    public int addMember(String mid, String name) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO chatmembers(mid, name) values(?, ?)";
    	Object[] args = {urlEncoded(mid), urlEncoded(name)};
    	return jdbcTemplate.update(sql,args);
    }

	public int deleteMember(String mid) throws UnsupportedEncodingException 
	{
		String sql = "DELETE FROM chatmembers WHERE mid = ?";
		Object[] args = {urlEncoded(mid)};
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
			
			encodedMap.put("mid", urlDecoded(String.valueOf(member.get("mid"))));
			encodedMap.put("name", urlDecoded(String.valueOf(member.get("name"))));
			retList.add(encodedMap);
		}
		return retList;
	}
	
	private String urlEncoded(String str) throws UnsupportedEncodingException 
	{
		String encStr = new String (URLEncoder.encode(str, "utf-8"));
		return encStr.trim();
	}
	private String urlDecoded(String str) throws UnsupportedEncodingException 
	{
		String decStr = new String (URLDecoder.decode(str, "utf-8"));
		return decStr.trim();
	}
}