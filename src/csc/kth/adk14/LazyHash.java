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
 *  K - the generated concordance (word : byte offset in S).
 *  K2 - distinct words with position of the first occurrence in E.
 *  E - /Everest/ a list of byte offsets in S sorted by word.
 *  L - the lazyhash lookup file (first three chars : byte offset in K2).
 *  
 * @author fsommar, miloszw
 *
 */
public class LazyHash {
	File lFile, k2File;

	public LazyHash(String lPath, String k2Path) {
		this.lFile = new File(lPath);
		this.k2File = new File(k2Path);
	}

	/**
	 * Parse the K2 file and generate the L file.
	 * TODO: Save the word indices as shorts instead of UTF strings
	 * @throws IOException
	 */
	public void generateFromFile() throws IOException {
		DataInputStream k2Reader = new DataInputStream(new BufferedInputStream(new FileInputStream(k2File)));
		DataOutputStream lWriter = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(lFile)));
		  
		String lastSaved = "";
		int posSize = Long.SIZE/8;
		int byteCounter = 0;

		try {
			while (true) {
				String currWord = k2Reader.readUTF();
				// Discard position in E
				k2Reader.readLong();
				String xxx = currWord.substring(0, Math.min(3, currWord.length()));
				
				if (!xxx.equals(lastSaved)) {
					// save word together with corresponding byte offset in K2
					lWriter.writeUTF(xxx);
					lWriter.writeLong(byteCounter);
					lastSaved = xxx;
				}
				// Inspect if encoding bug present with byte counts
				byteCounter += currWord.getBytes().length + posSize;
			}
		} catch (EOFException e) {
			
		}
		k2Reader.close();
		lWriter.close();
	}
	
	/**
	 * Create an array and populate it with data from L.
	 * 
	 * @return an array where each index corresponds to the unique
	 * hash value generated from the first three chars, and the value
	 * is the byte offset in S.
	 * @throws IOException
	 */
	public long[] readIndexArrFromFile() throws IOException {
		// 3 positions in base 30, with maximum value of 900*29+30*29+29
		// with one extra element for the EOF byte position
		long[] indexArr = new long[900*29+30*29+29+1]; 
		DataInputStream lReader = new DataInputStream(new BufferedInputStream(new FileInputStream(lFile)));
		
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
			indexArr[i] = k2File.length();
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
