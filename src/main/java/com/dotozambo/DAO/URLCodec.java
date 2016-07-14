package com.dotozambo.DAO;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.springframework.stereotype.Component;

@Component
public class URLCodec {

	public String urlEncoded(String str) throws UnsupportedEncodingException 
	{
		String encStr = new String (URLEncoder.encode(str, "utf-8"));
		return encStr.trim();
	}
	public String urlDecoded(String str) throws UnsupportedEncodingException 
	{
		String decStr = new String (URLDecoder.decode(str, "utf-8"));
		return decStr.trim();
	}
}
