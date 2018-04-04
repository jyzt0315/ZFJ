package com.zfj.dm.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLProcess {
	
	public String getPageContent(String strUrl, String strPostRequest) {  
        // 读取结果网页  
        StringBuffer buffer = new StringBuffer();  
        System.setProperty("sun.net.client.defaultConnectTimeout", "500000");  
        System.setProperty("sun.net.client.defaultReadTimeout", "500000");  
        try {  
            URL newUrl = new URL(strUrl);  
            HttpURLConnection hConnect = (HttpURLConnection) newUrl  
                    .openConnection();  
            // POST方式的额外数据  
            if (strPostRequest.length() > 0) {  
                hConnect.setDoOutput(true);  
                OutputStreamWriter out = new OutputStreamWriter(hConnect  
                        .getOutputStream());  
                out.write(strPostRequest);  
                out.flush();  
                out.close();  
            }  
            // 读取内容  
              
            BufferedReader rd = new BufferedReader(new InputStreamReader(  
                    hConnect.getInputStream(), "gbk"));
            int ch;
            do{
            	if((ch = rd.read()) <= -1) break;
            	buffer.append((char) ch);
            }while(true);
            String s = buffer.toString();  
            s.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
            
            rd.close();  
            hConnect.disconnect();  
            return buffer.toString().trim();  
        } catch (Exception e) {  
            // return "错误:读取网页失败！";  
            //  
            return null;    
        }  
    }
}
