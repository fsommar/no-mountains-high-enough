package csc.kth.adk14;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import sun.org.mozilla.javascript.ast.ForInLoop;

public class LazyHash {

	public static void parse() throws IOException {
		BufferedReader kReader = new BufferedReader(new FileReader(Constants.TEST_CASES_PATH));
		DataOutputStream lWriter = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(Constants.L_PATH)));
		  
		String lastSaved = "";
		String line;
		int byteCounter = 0;

		while ((line = kReader.readLine()) != null) {
			String[] data = line.split(" "); 
			String currWord = data[0];
			String xxx = currWord.substring(0, Math.min(3, currWord.length()));

			if (!xxx.equals(lastSaved)) {
				// save word together with corresponding byte offset in K
				lWriter.writeUTF(xxx);
				lWriter.writeInt(byteCounter);
				lastSaved = xxx;
			}
			byteCounter += line.getBytes().length;
		}
		kReader.close();
		lWriter.close();
	}

	public static long[] indexArrfromL(String path) throws IOException {
		// One extra element for the EOF byte position
		long[] indexArr = new long[900*29+30*29+29+1];
		File f = new File(path);
		DataInputStream lReader = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		
		try {
			for (int i = 0; i < indexArr.length; i++) {
				int currHash = hash(lReader.readUTF());
				int pos = lReader.readInt();
				
				indexArr[currHash] = pos;			

				// check if values exist for previous keys
				while (currHash > 0 && indexArr[currHash-1] == 0) {
					indexArr[currHash-1] = pos;
					currHash--;
				}
			}	
		} catch (EOFException e) {
			// Fallthrough
		}
		
		for (int i = indexArr.length-1; i >= 0 && indexArr[i] == 0; i--) {
			indexArr[i] = f.length();
		}
		
		lReader.close();

		return indexArr;
	}

	public static int hash(String str) {
		if (str == null) {
			return 0;
		}

		String xxx = str.toLowerCase().substring(0, Math.min(3, str.length()));
		char[] chars = xxx.toCharArray();
		int hashCode = 0;
		
		for (int i = 0; i < chars.length; i++) {
			hashCode += fromBase30(chars[i]) * Math.pow(30, chars.length-1-i);
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
