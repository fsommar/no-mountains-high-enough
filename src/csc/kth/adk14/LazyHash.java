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
import java.util.Arrays;

public class LazyHash {
	File lFile, k2File;
	private long[] indexArray;

	public LazyHash(String lPath, String k2Path) {
		this.lFile = new File(lPath);
		this.k2File = new File(k2Path);
	}

	/**
	 * Parse the K2 file and generate the L file.
	 * @throws IOException
	 */
	public void generateFromFile() throws IOException {
		// We use buffered streams because we will be traversing/writing the whole file, without skipping.
		BufferedReader k2Reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(k2File),"ISO-8859-1"));
		DataOutputStream lWriter = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(lFile)));
		  
		// Last written word to L.
		String lastSaved = "";
		
		// Used to keep track of current byte offset in K2.
		int byteCounter = 0;

		String line;
		while ((line = k2Reader.readLine()) != null) {				
			// Each line in K2 is of the form >> a unique word : byte offset in E (separated by a space).
			String[] lineData = line.split(" ");
			
			// We only store the word in L, together with corresponding position in K2.
			// Hence, we don't need lineData[1] which is the position in E.
			String word = lineData[0];
			String truncatedWord = word.substring(0, Math.min(3, word.length()));
			
			// If the first three characters of the word we are currently parsing are the same as
			// the previously saved word, we skip to next word (until a new unique three char combination is found)
			if (!truncatedWord.equals(lastSaved)) {
				// Write the hashed value of the first three characters. We store is as a short, since the hashed value
				// is < 30,000, which can be stored with 15 bits. A short is 16 bits.
				lWriter.writeShort(hash(truncatedWord));
				
				// Write the position of given word in K2. Since we only write the first occurrence of a unique three char
				// combination, the position will always point at the first occurrence of that combination in K2.
				lWriter.writeLong(byteCounter);
				lastSaved = truncatedWord;
			}
			// Increase byteCounter with the line size plus one for new line char.
			byteCounter += line.getBytes("ISO-8859-1").length + 1;
		}
		
		// Close file str34mz.
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
		
		// We initiate the array with -1 to indicate invalid position.
		// Will be later replaced by valid positions.
		Arrays.fill(indexArr, -1);
		
		// We use a buffered reader since we will be traversing the whole file (without skipping).
		DataInputStream lReader = new DataInputStream(new BufferedInputStream(new FileInputStream(lFile)));
		
		try {
			// We iterate through the entire array. During each iteration we read the word
			// hash and the corresponding position in K2.
			for (int i = 0; i < indexArr.length; i++) {
				int currHash = lReader.readShort();
				long pos = lReader.readLong();
				
				indexArr[currHash] = pos;			

				// Check if values exist for previous keys, if not - fill it with current value.
				// For instance if there is no word starting with 'aaa', we fill the position
				// value from 'aab' upwards, filling all "empty" (holding value -1) indexes.
				//
				// aaa -1   ^
				// aaa -1  ^ ^
				// aab 12 ^ ^ ^
				//
				while (currHash > 0 && indexArr[currHash-1] == -1) {
					indexArr[currHash-1] = pos;
					currHash--;
				}
			}	
		} catch (EOFException e) {
			// This is to be APPARENTLY expected since we will read through the L-file until EOF.
			// There's no way to check for end of file using DataInputStream so this will have to do.
			// As a precaution we only loop through the size of the index array as that is the maximum
			// possible amount of three-character combinations we'll have to read.
		}
		
		// We start from the very last index and fill the positions with the end of file position
		// until the next valid position.
		for (int i = indexArr.length-1; i >= 0 && indexArr[i] == -1; i--) {
			indexArr[i] = k2File.length();
		}
		
		lReader.close();

		// Save the index array as an instance variable so it can be reusable outside of this class.
		this.indexArray = indexArr;
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
		// Get the first three characters of the word.
		String truncatedWord = str.toLowerCase().substring(0, Math.min(3, str.length()));
		char[] chars = truncatedWord.toCharArray();
		int hashCode = 0;
		
		// Hash code will be c0 * 30^2 + c1 * 30^1 + c2 * 30^0 
		// where c0,c1,c2 are the first three characters.
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
			// If the character is in the English alphabet, subtract 96 (since
			// a in ASCII = 97) to get a = 1, b = 2, ..., z = 26.
			return c - 96;
		} 
		// Handle Swedish chars
		else if (c == 'å') {
			return 27;
		} else if (c == 'ä') {
			return 28;
		} else if (c == 'ö') {
			return 29;
		}		
		return 0;
	}

	public long[] getIndexArray() {
		return indexArray;
	}
}
