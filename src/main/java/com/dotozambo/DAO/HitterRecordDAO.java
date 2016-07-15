package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dotozambo.BO.URLCodecBO;
import com.dotozambo.Model.HitterRecord;

@Component
public class HitterRecordDAO 
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private URLCodecBO urlCodecBO;
	
    public int addHitterRecord(HitterRecord hr) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO hitterrecord([date], team_code, [name], stadium, [order], [position], num, record, ab, h, rbi, r, [avg]) "
    								+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	Object[] args = 
    		{
    			hr.getDate(), hr.getTeam_code(),
    			urlCodecBO.encode(hr.getName()), hr.getStadium(),
    			hr.getOrder(), urlCodecBO.encode(hr.getPosition()), hr.getNum(),
    			urlCodecBO.encode(hr.getRecord()),
    			hr.getAb(), hr.getH(), hr.getRbi(), hr.getR(), hr.getAvg()
    	};
    	
    	return jdbcTemplate.update(sql,args);
    }
}
