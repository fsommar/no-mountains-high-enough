package csc.kth.adk14;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LazyHash {
	
	public static void parse() throws IOException {
		BufferedReader kReader = new BufferedReader(new FileReader(Constants.TEST_CASES_PATH));
		BufferedWriter lWriter = new BufferedWriter(new FileWriter(Constants.L_PATH));
		String lastSaved = "";
		String line;

		while ((line = kReader.readLine()) != null) {
			String[] data = line.split(" ");
			String currWord = data[0];
			String xxx = currWord.substring(0, Math.min(3, currWord.length()));

			if (!xxx.equals(lastSaved)) {
				// save word
				lWriter.write(xxx+" "+data[1]+"\n");
				lastSaved = xxx;
			} else {
				continue;
			}			
		}
		kReader.close();
		lWriter.close();
	}
	
	public static int[] indexArrfromL() throws IOException {
		int[] indexArr = new int[900*29+30*29+29];
		BufferedReader lReader = new BufferedReader(new FileReader(Constants.L_PATH));
		String line;

		while ((line = lReader.readLine()) != null) {
			String[] data = line.split(" ");
			int currHash = hash(data[0]);
			int pos = Integer.parseInt(data[1].trim());
			
			indexArr[currHash] = pos;
			
			// if previous hash doesn't exist, then add this hash to it
			// this assumes the words are in a sorted order
			if (currHash > 1 && indexArr[currHash-1] == 0) {
				indexArr[currHash-1] = pos;
			}
		}
		
		return indexArr;
	}
	
	public static int[] readIndexArr() {
		return null;
	}
	
	public static void writeIndexArr(int[] indexArr) {
		// use DataStream
	}
	
	public static int hash(String str) {
		if (str == null) {
			return 0;
		}
		
		String xxx = str.toLowerCase().substring(0, Math.min(3, str.length()));
		char[] chars = xxx.toCharArray();
		int hashCode = 0;		
		
		for (int i = 0; i < chars.length; i++) {
			hashCode += fromBase30(chars[i]) * Math.pow(30, (2-i));
		}
		
		return hashCode;
	}
	
	public static int fromBase30(char c) {
		if ("abcdefghijgklmnopqrstuvwxyz".indexOf(c) != -1) {
			return c - 96;
		} else if (c == 'å') {
			return 27;
		} else if (c == 'ä') {
			return 28;
		} else if (c == 'ö') {
			return 29;
		}		
		return 0;
	}
}
