package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dotozambo.BO.URLCodecBO;
import com.dotozambo.Model.PitcherRecord;

@Component
public class PitcherRecordDAO 
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private URLCodecBO urlCodecBO;
	
    public int addPitcherRecord(PitcherRecord pr) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO pitcherrecord([date], team_code, [name], stadium, si, result, w, l, sv, ip, tbf, np, pa, h, hr, bb, so, r, er, era) "
    								+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	Object[] args = 
    		{
    			pr.getDate(), pr.getTeam_code(),
    			urlCodecBO.encode(pr.getName()), pr.getStadium(),
    			urlCodecBO.encode(pr.getSi()), urlCodecBO.encode(pr.getResult()),
    			pr.getW(), pr.getL(), pr.getSv(), pr.getIp(), pr.getTbf(), pr.getNp(), pr.getPa(),
    			pr.getH(), pr.getHr(), pr.getBb(), pr.getSo(), pr.getR(), pr.getEr(), pr.getEra()
    	};
    	
    	return jdbcTemplate.update(sql,args);
    }
    
    public String selectPitcherRecord(String sql) throws UnsupportedEncodingException {
    	String result = jdbcTemplate.queryForObject(sql, null, String.class);
    	return urlCodecBO.decode(result);
    }
}
