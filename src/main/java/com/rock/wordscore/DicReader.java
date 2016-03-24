package com.rock.wordscore;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DicReader {

	/**
	 * 加载词库内容
	 * @param dicPath
	 * @return
	 */
	
	public static String loadPosAndNegDic(String classPathDic){
		File f = new File(DicReader.class.getClassLoader()
				.getResource(classPathDic).getFile());
		StringBuilder sb = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String tempStr = null;
			sb = new StringBuilder();
			while((tempStr = br.readLine()) != null){
				sb.append(tempStr);
			}
			String result = sb.toString();
			return result.replaceAll(",+", "|");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br!=null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
}