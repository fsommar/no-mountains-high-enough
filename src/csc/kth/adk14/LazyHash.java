package csc.kth.adk14;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class LazyHash {

	public static void parse() throws IOException {
		BufferedReader kReader = new BufferedReader(new FileReader(Constants.TEST_CASES_PATH));
		BufferedWriter lWriter = new BufferedWriter(new FileWriter(Constants.L_PATH));
		String lastSaved = "";
		String line;
		int byteCounter = 0;

		while ((line = kReader.readLine()) != null) {
			String[] data = line.split(" ");
			String currWord = data[0];
			String xxx = currWord.substring(0, Math.min(3, currWord.length()));

			if (!xxx.equals(lastSaved)) {
				// save word together with corresponding byte offset in K
				lWriter.write(xxx+" "+byteCounter+"\n");
				lastSaved = xxx;
			}
			byteCounter += line.getBytes().length;
		}
		kReader.close();
		lWriter.close();
	}

	public static int[] indexArrfromL(String path) throws IOException {
		int[] indexArr = new int[900*29+30*29+29];
		BufferedReader lReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "ISO-8859-1"));
		String line;

		while ((line = lReader.readLine()) != null) {
			String[] data = line.split(" ");
			if (line.length() < 2) {
				continue;
			}
			int currHash = hash(data[0].trim());
			int pos = Integer.parseInt(data[1].trim());

			indexArr[currHash] = pos;

			// check if values exist for previous keys
			while (currHash > 0 && indexArr[currHash-1] == 0) {
				indexArr[currHash-1] = pos;
				currHash--;
			}
			
			// TODO: indexes following last valid word should point to last byte in file (EOF).
			
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
