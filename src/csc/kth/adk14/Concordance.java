package csc.kth.adk14;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;


public class Concordance {
	private Mountains concordance;
	private LazyHash lazyHash;
	
	public Concordance(Mountains concordance, LazyHash lazyHash) {
		this.concordance = concordance;
		this.lazyHash = lazyHash;
	}

	/**
	 * Use binary search to find a word and its corresponding range of positions in a {@link PositionRange} in the K2 file.
	 * @return the position of the word in the S file, or -1 if the word wasn't found.
	 */
	public PositionRange searchK2(String searchTerm)
			throws IOException {
		// Find byte offset of the word in K through L
		int hash = LazyHash.hash(searchTerm);
		
		long[] indexArray = lazyHash.getIndexArray();
		
		long startRange = indexArray[hash];
		long endRange = indexArray[hash+1];
		
		// Init a reader
		RandomAccessFile k2Reader = new RandomAccessFile(concordance.getK2(), "r");
		
		// Binary search the K file to find the byte offset of the word in S.		
		while (endRange-startRange > Constants.LINEAR_SEARCH_TRESHOLD) {
			long middle = (startRange + endRange) / 2;
			k2Reader.seek(middle);
			
			// skip to next line (in case we end up in the middle of a line)			
			k2Reader.readLine();
			
			LineData data = readLineDataFromFile(k2Reader);
			if (data == null) {
				break;
			}

			// If the word is less than or equal then continue binary search to da left
			// else to da rite
			if (data.word.compareTo(searchTerm) <= 0) {
				startRange = middle;
			} else {
				endRange = middle;
			}
		}
		
		// COMMENCE LINEAR SEARCH
		k2Reader.seek(startRange);
		if (startRange != indexArray[hash]) {
			System.out.println("Commencing linear search and skipping one line: "+k2Reader.readLine());
		}
		
		try {
			while (true) {
				LineData data = readLineDataFromFile(k2Reader);
				int wordComp = data.word.compareTo(searchTerm);
				if (wordComp == 0) {
					long nextPos = readLineDataFromFile(k2Reader).position;
					// Returns the position of the position tuple in Everest.
					return new PositionRange(data.position, nextPos);
				} else if (wordComp > 0) {
					System.out.println("went too far in searchK2: "+data.word);
					break;
				}
			}
			return null;
		} finally {
			k2Reader.close();
		}
	}
	
	public static LineData readLineDataFromFile(DataInput fileInput) throws IOException {
		String line = fileInput.readLine();
		if (line == null) {
			return null;
		}

		String[] lineData = line.split(" ");
		String word = lineData[0].trim();
		
		return new LineData(word, Long.valueOf(lineData[1]));
	}
	
	public static class LineData {
		public final String word;
		public final long position;
		
		public LineData(String word, long position) {
			this.word = word;
			this.position = position;
		}
	}

	
	public static class PositionRange {
		public final long start, end;
		public PositionRange(long start, long end) {
			this.start = start;
			this.end = end;
		}
	}
	
}
