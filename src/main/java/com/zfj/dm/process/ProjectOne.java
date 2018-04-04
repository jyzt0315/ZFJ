package com.zfj.dm.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zfj.dm.utils.TXTProcess;


public class ProjectOne {
	public static void main(String[] args) throws IOException{
		
		ObjectMapper map = new ObjectMapper();
		TXTProcess tp = new TXTProcess();
		String date = "20180330";
		ArrayList<String> data = tp.readFileByLines("/Users/hehuan/Desktop/zfj/trade" + date + ".txt");
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		for(String mid : data) {
			String[] codeInfo = mid.split("========");
			if(codeInfo.length == 2){
				List<Map<String, Object>> codeData = map.readValue(codeInfo[1], new TypeReference<List<Map<String, Object>>>() {});
				if (codeData.size() > 0) {
					List<List<String>> codeList = map.readValue(codeData.get(0).get("hq").toString().replaceAll("-", "").replaceAll("%", "").replaceAll(", ]", "]"), new TypeReference<List<List<String>>>() {});
					List<Double> diff = new ArrayList<Double>();
					List<Double> amount = new ArrayList<Double>();
					Boolean a = true;
					if(!codeList.get(0).get(0).equals(date)){
						a = false;
					}
					for (List<String> midList : codeList){
						diff.add(Double.parseDouble(midList.get(2)) - Double.parseDouble(midList.get(1)));
						amount.add(Double.parseDouble(midList.get(8)));
					}
					if (diff.size() > 60){
						for(int i = 0; i < Math.min(5, diff.size()); i++) {
							if (diff.get(i) >= 0) {
								a = false;
							}
						}
						if (a == true) {
							List<Double> result = amount.subList(0, Math.min(amount.size(), 3));
							List<String> result2 = new ArrayList<String>();
							for(Double resultMid : result) {
								result2.add(df.format(resultMid/result.get(result.size() - 1)));
							}
							System.out.println(codeInfo[0] + "====" + result2);
						}
					}
				}
			}
			
		}
	}
}
