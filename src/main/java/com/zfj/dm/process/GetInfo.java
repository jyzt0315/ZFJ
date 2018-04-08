package com.zfj.dm.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zfj.dm.utils.URLProcess;

public class GetInfo {
	public static void main(String[] args) throws IOException{
		URLProcess up = new URLProcess();
		String data = up.getPageContent("http://quote.eastmoney.com/stocklist.html", "");
        String regex = "(?<=\\().*(?=\\))";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(data);
        String date = "20180404";
        //http://q.stock.sohu.com/hisHq?code=cn_600420
        String codeUrl = "http://q.stock.sohu.com/hisHq?code=cn_";
        @SuppressWarnings("resource")
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/Users/hehuan/Desktop/zfj/trade" + date + ".txt"), false));
        while (m.find()) {
        	String mid = m.group();
        	if(mid.length() == 6){
        		String tradeData = up.getPageContent(codeUrl + mid, "");
        		if (tradeData.length() > 100) {
        			writer.write(mid + "========" + tradeData + "\n");
        		}
        	}
        }
        writer.flush();
	}
}
