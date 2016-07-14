package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dotozambo.Model.HitterRecord;

@Component
public class HitterRecordDAO 
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private URLCodec urlCodec;
	
    public int addHitterRecord(HitterRecord hr) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO hitterrecord([date], team_code, [name], stadium, [order], [position], num, record, ab, h, rbi, r, [avg]) "
    								+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	
    	Object[] args = 
    		{
    			hr.getDate(), hr.getTeam_code(),
    			urlCodec.urlEncoded(hr.getName()), hr.getStadium(),
    			hr.getOrder(), urlCodec.urlEncoded(hr.getPosition()), hr.getNum(),
    			urlCodec.urlEncoded(hr.getRecord()),
    			hr.getAb(), hr.getH(), hr.getRbi(), hr.getR(), hr.getAvg()
    	};
    	
    	return jdbcTemplate.update(sql,args);
    }
}
