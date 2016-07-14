package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dotozambo.Model.PitcherRecord;

@Component
public class PitcherRecordDAO 
{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private URLCodec urlCodec;
	
    public int addPitcherRecord(PitcherRecord pr) throws UnsupportedEncodingException 
    {
    	String sql = "INSERT INTO pitcherrecord([date], team_code, [name], stadium, si, result, w, l, sv, ip, tbf, np, pa, h, hr, bb, so, r, er, era) "
    								+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	
    	Object[] args = 
    		{
    			pr.getDate(), pr.getTeam_code(),
    			urlCodec.urlEncoded(pr.getName()), pr.getStadium(),
    			urlCodec.urlEncoded(pr.getSi()), urlCodec.urlEncoded(pr.getResult()),
    			pr.getW(), pr.getL(), pr.getSv(), pr.getIp(), pr.getTbf(), pr.getNp(), pr.getPa(),
    			pr.getH(), pr.getHr(), pr.getBb(), pr.getSo(), pr.getR(), pr.getEr(), pr.getEra()
    	};
    	
    	return jdbcTemplate.update(sql,args);
    }
}
