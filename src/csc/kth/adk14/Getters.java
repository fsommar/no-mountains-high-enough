package csc.kth.adk14;

import java.io.RandomAccessFile;

public class Getters {
	/**
	 * Use binary search to find a word and its corresponding position in the K file.
	 * @param byteOffsetA Start range
	 * @param byteOffsetB End range
	 * @param path The path to the K file.
	 * @return the position of the word in the S file, or -1 if the word wasn't found.
	 */
	public static long searchK(String word, long[] indexArray, String path)
			throws Exception {
		// Find byte offset of the word in K through L
		int hash = LazyHash.hash(word);
		
		long startRange = indexArray[hash];
		long endRange = indexArray[hash+1];
		
		// Init a reader
		RandomAccessFile kReader = new RandomAccessFile(path, "r");
		
		// Binary search the K file to find the byte offset of the word in S.		
		while (endRange-startRange > 10) {
			long middle = (startRange + endRange) / 2;
			kReader.seek(middle);
			
			// skip to next line (in case we end up in the middle of a line)			
			kReader.readLine();
			String line = kReader.readLine();
			if (line == null) {
				break;
			}

			String[] middleLine = line.split(" ");
			String middleWord = middleLine[0].trim();
			// If the word is less than or equal then continue binary search to da left
			// else to da rite
			if (middleWord.compareTo(word) <= 0) {
				startRange = middle;
			} else {
				endRange = middle;
			}
		}
		
		// COMMENCE LINEAR SEARCH
		kReader.seek(startRange);
		kReader.readLine(); // !! make sure we dont miss the first word
		
		while (true) {
			String line = kReader.readLine();
			if (line == null) {
				break;
			}
			
			String[] middleLine = line.split(" ");
			String middleWord = middleLine[0].trim();
			
			int wordComp = middleWord.compareTo(word);
			if (wordComp == 0) {
				// TODO: Add values to an ArrayList and return that mofo
				return Long.valueOf(middleLine[1]);
			} else if (wordComp > 0) {
				break;
			}
		}
		return -1;
	}
}
