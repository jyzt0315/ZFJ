package com.zfj.dm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TXTProcess {
	public ArrayList<String> readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        ArrayList<String> data = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            int line = 1;
            
            while ((tempString = reader.readLine()) != null) {
                data.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return data;
    }
}
