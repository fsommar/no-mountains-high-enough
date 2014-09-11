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
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * We'll be working with three files:
 * 	S - the source.
 *  K - the concordance (word : byte offset in S)
 *  L - the lazyhash lookup file (first three chars : byte offset in K)
 *  
 * @author fsommar, miloszw
 *
 */
public class LazyHash {

	/**
	 * Parse the K file and generate the L file.
	 * @throws IOException
	 * @param input the K file
	 * @param output the L file
	 */
	public static void parse(String input, String output) throws IOException {
		BufferedReader kReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(input), "ISO-8859-1"));
		DataOutputStream lWriter = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(output)));
		  
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
				lWriter.writeLong(byteCounter);
				lastSaved = xxx;
			}
			byteCounter += line.getBytes().length;
		}
		kReader.close();
		lWriter.close();
	}
	
	/**
	 * Create an array and populate it with data from L.
	 * 
	 * @param lPath path to L.
	 * @return an array where each index corresponds to the unique
	 * hash value generated from the first three chars, and the value
	 * is the byte offset in S.
	 * @throws IOException
	 */
	public static long[] indexArrfromL(String lPath, String kPath) throws IOException {
		// One extra element for the EOF byte position
		long[] indexArr = new long[900*29+30*29+29+1];
		DataInputStream lReader = new DataInputStream(new BufferedInputStream(new FileInputStream(lPath)));
		File kFile = new File(kPath);
		
		try {
			for (int i = 0; i < indexArr.length; i++) {
				int currHash = hash(lReader.readUTF());
				long pos = lReader.readLong();
				
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
			indexArr[i] = kFile.length();
		}
		
		lReader.close();

		return indexArr;
	}

	/**
	 * Generate a hash value based on the first three chars.
	 * @param str the word.
	 * @return hashed value of the first three chars of the word.
	 */
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
	
	/**
	 * Convert the character code from the Swedish alphabet to base 30.
	 * @param c the char.
	 * @return the base 30 repr of the char (duuuh).
	 */
	private static int fromBase30(char c) {
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
