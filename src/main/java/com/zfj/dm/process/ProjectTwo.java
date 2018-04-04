package com.zfj.dm.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zfj.dm.utils.TXTProcess;

import weka.classifiers.functions.Logistic;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class ProjectTwo {
	public static void main(String[] args) throws Exception{
		
		ObjectMapper map = new ObjectMapper();
		TXTProcess tp = new TXTProcess();
		int attributeNum = 15;
		String date = "20180403";
		String arffTrain = "/Users/hehuan/Desktop/zfj/train.arff";
		String arffTest = "/Users/hehuan/Desktop/zfj/test.arff";
		ArrayList<String> data = tp.readFileByLines("/Users/hehuan/Desktop/zfj/trade" + date + ".txt");
		@SuppressWarnings("resource")
		BufferedWriter writerTrain = new BufferedWriter(new FileWriter(new File(arffTrain), false));
		@SuppressWarnings("resource")
		BufferedWriter writerTest = new BufferedWriter(new FileWriter(new File(arffTest), false));
		
		writerTrain.write("@RELATION TrainValueSet"+"\n");
		for(int i=0;i<attributeNum;i++){
			writerTrain.write("@ATTRIBUTE"+"\t"+"A"+i+"\t"+"NUMERIC"+"\n");
		}
		writerTrain.write("@ATTRIBUTE"+"\t"+"A"+attributeNum+"\t"+"{0,1}"+"\n");
		writerTrain.write("@DATA"+"\n");
		
		writerTest.write("@RELATION TestValueSet"+"\n");
		for(int i=0;i<attributeNum;i++){
			writerTest.write("@ATTRIBUTE"+"\t"+"A"+i+"\t"+"NUMERIC"+"\n");
		}
		writerTest.write("@ATTRIBUTE"+"\t"+"A"+attributeNum+"\t"+"NUMERIC"+"\n");
		writerTest.write("@DATA"+"\n");

		Comparator<Double> idComparator = new Comparator<Double>(){
			
	    	@Override
	        public int compare(Double c1, Double c2) {
	        	if(c1 - c2 > 0) return 1;
	        	return -1;
	        }
	    };
	    
		Comparator<Double> idComparatorD = new Comparator<Double>(){
			
	    	@Override
	        public int compare(Double c1, Double c2) {
	        	if(c2 - c1 > 0) return 1;
	        	return -1;
	        }
	    };

		Queue<Double> doublePriorityQueueAsc = new PriorityQueue<>(1, idComparator);
		
		for(String mid : data) {
			String[] codeInfo = mid.split("========");
			if(codeInfo.length == 2 && (codeInfo[0].startsWith("0") || codeInfo[0].startsWith("3") || codeInfo[0].startsWith("6"))){
				List<Map<String, Object>> codeData = map.readValue(codeInfo[1], new TypeReference<List<Map<String, Object>>>() {});
				if (codeData.size() > 0) {
					List<List<String>> codeList = map.readValue(codeData.get(0).get("hq").toString().replaceAll("-", "").replaceAll("%", "").replaceAll(", ]", "]"), new TypeReference<List<List<String>>>() {});
					List<Double> price = new ArrayList<Double>();
					List<Double> amount = new ArrayList<Double>();
					if(codeList.get(0).get(0).equals(date)){
						for (List<String> midList : codeList){
							price.add(Double.parseDouble(midList.get(2)));
							amount.add(Double.parseDouble(midList.get(8)));
						}
						if (price.size() > 10 && amount.get(2) > 0  && amount.get(3) > 0){
							Double score = price.get(0)/price.get(1);
							doublePriorityQueueAsc.offer(score);
						}
					}
				}
			}
		}
		
		Double dataSize = (double)doublePriorityQueueAsc.size();
		int positiveSize = (int) Math.round(dataSize * 0.1);
		int negitiveSize = (int) Math.round(dataSize * 0.3);
		List<Double> positiveList = new ArrayList<>();
		List<Double> negitiveList = new ArrayList<>();
		for(int i = 0; i < dataSize; i++){
			if(i < negitiveSize) {
				negitiveList.add(doublePriorityQueueAsc.poll());
			}
			if(dataSize - i < positiveSize) {
				positiveList.add(doublePriorityQueueAsc.poll());
			}
		}
		
		for(String mid : data) {
			String[] codeInfo = mid.split("========");
			if(codeInfo.length == 2 && (codeInfo[0].startsWith("0") || codeInfo[0].startsWith("3") || codeInfo[0].startsWith("6"))){
				List<Map<String, Object>> codeData = map.readValue(codeInfo[1], new TypeReference<List<Map<String, Object>>>() {});
				if (codeData.size() > 0) {
					List<List<String>> codeList = map.readValue(codeData.get(0).get("hq").toString().replaceAll("-", "").replaceAll("%", "").replaceAll(", ]", "]"), new TypeReference<List<List<String>>>() {});
					List<Double> price = new ArrayList<Double>();
					List<Double> amount = new ArrayList<Double>();
					List<Double> change = new ArrayList<Double>();
					if(codeList.get(0).get(0).equals(date)){
						for (List<String> midList : codeList){
							if(midList.size() >= 10){
								price.add(Double.parseDouble(midList.get(2)));
								amount.add(Double.parseDouble(midList.get(8)));
								change.add(Double.parseDouble(midList.get(9)));
							}
							
						}
						if (price.size() > 10 && amount.get(2) > 0  && amount.get(3) > 0){
							Double labelScore = price.get(0)/price.get(1);
							if(negitiveList.contains(labelScore) || positiveList.contains(labelScore)) {

								int label = 0;
								if(positiveList.contains(labelScore)) label = 1;
								Double priceDiffTrain1 = (price.get(1) - price.get(2)) / price.get(2);
								Double priceDiffTrain2 = (price.get(2) - price.get(3)) / price.get(3);
								Double priceDiffTrain3 = (price.get(3) - price.get(4)) / price.get(4);
								Double priceDiffTrain4 = (price.get(4) - price.get(5)) / price.get(5);
								Double priceDiffTrain5 = (price.get(5) - price.get(6)) / price.get(6);							
								
								Double amountDiffTrain1 = amount.get(1) / amount.get(5);
								Double amountDiffTrain2 = amount.get(2) / amount.get(5);
								Double amountDiffTrain3 = amount.get(3) / amount.get(5);
								Double amountDiffTrain4 = amount.get(4) / amount.get(5);
								Double amountDiffTrain5 = amount.get(5) / amount.get(5);
								
								Double changeTrain1 = change.get(1) - change.get(2);
								Double changeTrain2 = change.get(2) - change.get(3);
								Double changeTrain3 = change.get(3) - change.get(4);
								Double changeTrain4 = change.get(4) - change.get(5);
								Double changeTrain5 = change.get(5) - change.get(6);
								
								writerTrain.write(priceDiffTrain1 + "," + priceDiffTrain2 + "," + priceDiffTrain3 + "," + priceDiffTrain4 + "," + priceDiffTrain5 + ","
										         + amountDiffTrain1 + "," + amountDiffTrain2 + "," + amountDiffTrain3 + "," + amountDiffTrain4 + "," + amountDiffTrain5 + "," 
						                         + changeTrain1 + "," + changeTrain2 + "," + changeTrain3 + "," + changeTrain4 + "," + changeTrain5 + ","
										         + label + "\n");
							
							}
							Double priceDiffTest1 = (price.get(0) - price.get(1)) / price.get(1);
							Double priceDiffTest2 = (price.get(1) - price.get(2)) / price.get(2);
							Double priceDiffTest3 = (price.get(2) - price.get(3)) / price.get(3);
							Double priceDiffTest4 = (price.get(3) - price.get(4)) / price.get(4);
							Double priceDiffTest5 = (price.get(4) - price.get(5)) / price.get(5);
							
							Double amountDiffTest1 = amount.get(0) / amount.get(4);
							Double amountDiffTest2 = amount.get(1) / amount.get(4);
							Double amountDiffTest3 = amount.get(2) / amount.get(4);
							Double amountDiffTest4 = amount.get(3) / amount.get(4);
							Double amountDiffTest5 = amount.get(4) / amount.get(4);
							
							Double changeTest1 = change.get(0) - change.get(1);
							Double changeTest2 = change.get(1) - change.get(2);
							Double changeTest3 = change.get(2) - change.get(3);
							Double changeTest4 = change.get(3) - change.get(4);
							Double changeTest5 = change.get(4) - change.get(5);
							
							writerTest.write(priceDiffTest1 + "," + priceDiffTest2 + "," + priceDiffTest3 + ","  + priceDiffTest4 + "," + priceDiffTest5 + ","
							          + amountDiffTest1 + "," + amountDiffTest2 + "," + amountDiffTest3 + "," + amountDiffTest4 + "," + amountDiffTest5 + ","
							          + changeTest1 + "," + changeTest2 + "," + changeTest3 + "," + changeTest4 + "," + changeTest5 + ","
							          + codeInfo[0] + "\n");
						}
					}
				}
			}
		}
		writerTest.flush();
		writerTrain.flush();

		//NaiveBayes logic = trainModelNB(arffTrain);
		//Logistic logic = trainModel(arffTrain);
		//BayesNet logic = trainModelBN(arffTrain);
		Logistic model = trainModel(arffTrain);
		NaiveBayes model2 = trainModelNB(arffTrain);
		SMO model3 = trainModelSMO(arffTrain);

        ArffLoader loader = new ArffLoader();
        loader.setFile(new File(arffTrain));
        Instances insTest =loader.getDataSet();
        insTest.setClassIndex(insTest.numAttributes() - 1);
        double sum = insTest.numInstances();
        double right = 0.0;
        double positive = 100;
        
        double rate1 = 0.1;
        double rate2 = 0.8;
        double rate3 = 0.1;
        
        System.out.println(insTest.numInstances());
        
        Queue<Double> doublePriorityQueueDesc = new PriorityQueue<>(1, idComparatorD);
        Map<Double, Integer> dataScore = new HashMap<Double, Integer>();
        for(int i=0;i<sum;i++){
            Instance ins = insTest.instance(i);
            double[] a = model.distributionForInstance(ins);
            double[] b = model2.distributionForInstance(ins);
            double[] c = model3.distributionForInstance(ins);
            double score = a[1] * rate1 + b[1] * rate2 + c[1] * rate3;
            doublePriorityQueueDesc.offer(score);
            if(dataScore.containsKey(score)){
            	Integer midScore = dataScore.get(score);
            	dataScore.put(score, (midScore + (int)ins.classValue()));
            } else {
            	dataScore.put(score, (int)ins.classValue());
            }
        }

        for(int i = 0; i < positive; i++){
        	double score = doublePriorityQueueDesc.poll();
        	right += dataScore.get(score);
        }
        
        // 打印出分类的精确度
        System.out.println("classification precision:" + (right/positive));
        System.out.println("right:" + right);
        System.out.println("positive:" + positive);
        
        
        HashMap<Double,Double> mapData = new HashMap<Double,Double>();  
        ValueComparator bvc =  new ValueComparator(mapData);  
        TreeMap<Double,Double> sorted_map = new TreeMap<Double,Double>(bvc);  
        
        loader.setFile(new File(arffTest));
        insTest =loader.getDataSet();
        insTest.setClassIndex(insTest.numAttributes() - 1);
        sum = insTest.numInstances();
        right = 0.0;
        positive = 0.0;
        //threshold = 0.5;
        for(int i=0;i<sum;i++){
            Instance ins = insTest.instance(i);
            double[] a = model.distributionForInstance(ins);
            double[] b = model2.distributionForInstance(ins);
            double[] c = model3.distributionForInstance(ins);
            mapData.put(ins.classValue(), (a[1] + b[1]) / 2);
        }
        
        sorted_map.putAll(mapData);  
        
        Set<Map.Entry<Double, Double>> set = sorted_map.entrySet();
        System.out.println("result=================");
        int i = 0;
        for (Iterator<Map.Entry<Double, Double>> it = set.iterator(); it.hasNext();) {
        	i ++;
        	if (i > 10) break;
            Map.Entry<Double, Double> entry = (Map.Entry<Double, Double>) it.next();
            System.out.println(Integer.parseInt(new java.text.DecimalFormat("0").format(entry.getKey())) + "--->" + entry.getValue());
        }
		
	}
	
    static Logistic trainModel(String arffFile) throws Exception {

        File inputFile = new File(arffFile);
        ArffLoader loader = new ArffLoader();
        loader.setFile(inputFile);
        Instances insTrain = loader.getDataSet();
        insTrain.setClassIndex(insTrain.numAttributes() - 1);
        Logistic logic=new Logistic();
        logic.buildClassifier(insTrain);

        return logic;
    }
    
    static NaiveBayes trainModelNB(String arffFile) throws Exception {

        File inputFile = new File(arffFile);
        ArffLoader loader = new ArffLoader();
        loader.setFile(inputFile);
        Instances insTrain = loader.getDataSet();
        insTrain.setClassIndex(insTrain.numAttributes() - 1);
        NaiveBayes nb=new NaiveBayes();
        nb.buildClassifier(insTrain);

        return nb;
    }
    
    static SMO trainModelSMO(String arffFile) throws Exception {

        File inputFile = new File(arffFile);
        ArffLoader loader = new ArffLoader();
        loader.setFile(inputFile);
        Instances insTrain = loader.getDataSet();
        insTrain.setClassIndex(insTrain.numAttributes() - 1);
        SMO nb=new SMO();
        nb.buildClassifier(insTrain);

        return nb;
    }
    
    static BayesNet trainModelBN(String arffFile) throws Exception {

        File inputFile = new File(arffFile);
        ArffLoader loader = new ArffLoader();
        loader.setFile(inputFile);
        Instances insTrain = loader.getDataSet();
        insTrain.setClassIndex(insTrain.numAttributes() - 1);
        BayesNet nb=new BayesNet();
        nb.buildClassifier(insTrain);

        return nb;
    }   
}

class ValueComparator implements Comparator<Double> {  
	  
    Map<Double, Double> base;  
    public ValueComparator(Map<Double, Double> base) {  
        this.base = base;  
    }  
  
    // Note: this comparator imposes orderings that are inconsistent with equals.      
    public int compare(Double a, Double b) {  
        if (base.get(a) >= base.get(b)) {  
            return -1;  
        } else {  
            return 1;  
        } // returning 0 would merge keys  
    }  
}  