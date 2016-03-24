package com.rock.wordscore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyWordScore {
	
	private double d = 1.0;
	private double weight = 3.0;
	private double score = 0.0;
	//前置加分词
	private static String posiStr_pre;
	//后置加分词
	private static String posiStr_suf;
	//前置减分词
	private static String negStr_pre;
	//后置减分词
	private static String negStr_suf;
	private static Pattern posiPrePatt;
	private static Pattern posiSufPatt;
	private static Pattern negPrePatt;
	private static Pattern negSufPatt;
	private static String keyWordsStr;
	/**
	 * 关键词分数计算公式:
	 * score = d/(distance+1) * weight
	 * distance是关键词到加(减)分词的作用
	 * weight代表加(减)分词的权重值
	 */
	
	/**
	 * 加载关键字.加分词.减分词词库
	 * 创建正则匹配表达式对象
	 * KeyWords：存储关键字的词库
	 * BonusDic_Pre:前置加分词词库
	 * BonusDic_Suf:后置加分词词库
	 * DeductDic_Pre:前置减分词词库
	 * DeductDic_Suf:后置减分词词库
	 */
	static {
		keyWordsStr = DicReader.loadPosAndNegDic("KeyWords");
		posiStr_pre = DicReader.loadPosAndNegDic("BonusDic_Pre");
		posiStr_suf = DicReader.loadPosAndNegDic("BonusDic_Suf");
		negStr_pre = DicReader.loadPosAndNegDic("DeductDic_Pre");
		negStr_suf = DicReader.loadPosAndNegDic("DeductDic_Suf");
		
		posiPrePatt = Pattern.compile(posiStr_pre);
		posiSufPatt = Pattern.compile(posiStr_suf);
		negPrePatt = Pattern.compile(negStr_pre);
		negSufPatt = Pattern.compile(negStr_suf);
	}
	
	/**
	 * 计算关键词分数前的准备
	 */
	public void calculateScorePre(String text,String key,boolean isPosiWord,Map<String,Double> wordScoreMap){
		int keywordIndex = text.indexOf(key);
		int wordIndex = 0;
		if(keywordIndex >= 0){
			if(isPosiWord){
				calculateScore(text,key,wordIndex,keywordIndex+key.length(),isPosiWord,posiPrePatt,wordScoreMap);
				calculateScore(text,key,keywordIndex,text.length(),isPosiWord,posiSufPatt,wordScoreMap);
			}else{
				calculateScore(text,key,wordIndex,keywordIndex+key.length(),isPosiWord,negPrePatt,wordScoreMap);
				calculateScore(text,key,keywordIndex,text.length(),isPosiWord,negSufPatt,wordScoreMap);	
			}
		}
	}
	
	/**
	 * 关键词得分的详细计算
	 */
	public void calculateScore(String text,String key,int startPos,int endPos,boolean isPosi,Pattern patt,Map<String,Double> wordScoreMap){
		String tmp = text.substring(startPos, endPos);
		Matcher matcher = patt.matcher(tmp);
		String word = null;
		int distance = 0;
		int wordIndex = 0;
		int keywordIndex = text.indexOf(key);
		while(matcher.find()){
			word = matcher.group();
			wordIndex = text.indexOf(word);
			if(wordIndex <= keywordIndex)
				distance = keywordIndex - (wordIndex+word.length());
			else
				distance = wordIndex - (keywordIndex+key.length());
			score = d/(distance+1) * weight;
			if(isPosi)
				score = wordScoreMap.get(key) + score;
			else
				score = wordScoreMap.get(key) - score;
			wordScoreMap.put(key, score);
		}		
	}
	
	public Map<String,Double> sentenceFetch(String sentence,String keyWordsStr){
		Map<Integer,String> keyWordIndexMap = new HashMap<Integer,String>();
		Map<String,Double> wordScoreMap = new HashMap<String,Double>();
		Pattern fetchKeyPatt = Pattern.compile(keyWordsStr);
		Matcher matcher = fetchKeyPatt.matcher(sentence);
		String keyWord = null;
		while(matcher.find()){
			double score = 1;
			keyWord = matcher.group();
			if(wordScoreMap.containsKey(keyWord))
					score = wordScoreMap.get(keyWord) + 1;
			wordScoreMap.put(keyWord, score);
		}
		if(wordScoreMap.size()<=1)
			return wordScoreMap;
		List<Integer> wordIndexList = new ArrayList<Integer>();
		Iterator<?> iter = wordScoreMap.entrySet().iterator();
		while(iter.hasNext()){
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)iter.next();
			String keyW = (String)entry.getKey();
			int num = Integer.parseInt(new java.text.DecimalFormat("0").format(entry.getValue()));
			int beginIndex = 0;
			for(int i=0;i<num;i++){
				int posIndex = sentence.indexOf(keyW,beginIndex);
				keyWordIndexMap.put(posIndex,keyW);
				wordIndexList.add(posIndex);
				beginIndex = posIndex + keyW.length();
			}
		}
		Collections.sort(wordIndexList);
		phraseFetch(wordIndexList, sentence,keyWordIndexMap,wordScoreMap);
		return wordScoreMap;
	}
	
	
	/**
	 * 提取包含关键字的句子
	 */
	public void phraseFetch(List<Integer> wordIndexList,String sentence,Map<Integer,String> keyWordIndexMap,Map<String,Double>  wordScoreMap){
		int beginIndex = 0;
		int size = wordIndexList.size();
		String tmp = null;
		for(int i=0;i<size;i++){
			int endIndex = 0;
			if(i+1 < size){
				endIndex = wordIndexList.get(i+1);
			}else{
				beginIndex = wordIndexList.get(i-1)+ keyWordIndexMap.get(wordIndexList.get(i-1)).length();
				endIndex = sentence.length();
			}
			tmp = sentence.substring(beginIndex, endIndex);
			int currKeyWordIndex  = wordIndexList.get(i);
			beginIndex = currKeyWordIndex + keyWordIndexMap.get(currKeyWordIndex).length();
			//计算该关键字两端加(减)分词的得分
			calculateScorePre(tmp, keyWordIndexMap.get(wordIndexList.get(i)),true, wordScoreMap);
			calculateScorePre(tmp, keyWordIndexMap.get(wordIndexList.get(i)),false, wordScoreMap);
		}
	}
	
	public static void main(String[] agrs){
		KeyWordScore textScore = new KeyWordScore();
		String sentence = "电脑异响，怀疑是硬盘有问题，查为风扇的故障，更换风扇后正常";
		Map<String,Double>  map = textScore.sentenceFetch(sentence, keyWordsStr);
		Iterator<?> iterator = map.entrySet().iterator();
		while(iterator.hasNext()){
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)iterator.next();
			System.out.println("KeyWord:"+entry.getKey()+" Score:"+entry.getValue());
		}
	}
}