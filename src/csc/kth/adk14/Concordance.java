package csc.kth.adk14;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;


public class Concordance {
	private Mountains mountains;
	private LazyHash lazyHash;
	private String sPath;
	
	public Concordance(Mountains mountains, LazyHash lazyHash, String sPath) throws IOException {
		this.mountains = mountains;
		this.lazyHash = lazyHash;
		this.sPath = sPath;
		
		// Make sure to read the index array into memory from file to be able to use it in searches.
		lazyHash.readIndexArrFromFile();
	}

	/**
	 * Use binary search to find a word and its corresponding range of positions in a {@link PositionRange} in the K2 file.
	 * @return the position of the word in the S file.
	 * @throws WordNotFoundException if the word is not found in the source file.
	 */
	public PositionRange searchK2(String searchTerm)
			throws IOException, WordNotFoundException {
		
		searchTerm = searchTerm.toLowerCase();
		int hash = LazyHash.hash(searchTerm);
		
		long[] indexArray = lazyHash.getIndexArray();
		
		// The range is between the first three characters of the word until the next three-letter combination.
		long startRange = indexArray[hash];
		long endRange = indexArray[hash+1];
		
		// Initialize a stream for random access for seeking around in the file
		// without having to keep it in memory.
		RandomAccessFile k2Reader = new RandomAccessFile(mountains.getK2(), "r");
		
		// Binary search the K2 file to find the byte offset of the word in S.
		// Start linear searching after the threshold is reached and binary search
		// no longer is as effective.
		while (endRange-startRange > Constants.LINEAR_SEARCH_THRESHOLD) {
			long middle = (startRange + endRange) / 2;
			k2Reader.seek(middle);
			
			// skip to next line (in case we end up in the middle of a line)		
			k2Reader.readLine();
			
			// Read the current line
			LineData data = readLineDataFromFile(k2Reader);
			if (data == null) {
				// This will be null if the line isn't found,
				// most likely because of EOF.
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
		// If startRange hasn't range it means that we haven't jumped in the file
		// and therefore are in the beginning of a line.
		if (startRange != indexArray[hash]) {
			k2Reader.readLine();
		}
		
		try {
			while (true) {
				LineData data = readLineDataFromFile(k2Reader);
				if (data == null) {
					break;
				}
				int wordComp = data.word.compareTo(searchTerm);
				if (wordComp == 0) { // the words are equal
					// Read the next position to get a range for which to look in the E file.
					long nextPos = readLineDataFromFile(k2Reader).position;
					// Returns the position of the position tuple in Everest.
					return new PositionRange(data.word, data.position, nextPos);
				} else if (wordComp > 0) { 
					// data.word is greater than the searched term. This means
					// we have gone too far in the file meaning the word does
					// not exist.
					break;
				}
			}
			throw new WordNotFoundException();
		} finally {
			k2Reader.close();
		}
	}
	
	/**
	 * The range is assumed to be given at valid positions in the file.
	 * @param range The range at which to get the byte offsets from.
	 * @return A list of all the byte offsets in S given by the provided {@link PositionRange}.
	 */
	public ArrayList<Long> climbEverest(PositionRange range) throws IOException {
		RandomAccessFile eReader = new RandomAccessFile(mountains.getEverest(), "r");
		// Seek to the start of the range which is exactly where that range starts.
		eReader.seek(range.start);
		ArrayList<Long> results = new ArrayList<Long>();
		
		// Read while still not at the end of the range.
		while (eReader.getFilePointer() < range.end) {
			results.add(eReader.readLong());
		}
		eReader.close();
		return results;
	}
	
	/**
	 * Gets the the search term from the source file including the surrounding context.
	 * 
	 * @param offsetsInE
	 * @param wordLength the length of the search term in bytes.
	 * @return An array of the surrounding context of the word, one String per each word location.
	 * @throws IOException Probably in case the S-file doesn't exist, but don't quote me on that one.
	 */
	public String[] getContextArrayFromFile(ArrayList<Long> offsetsInE, int wordLength) throws IOException {
		RandomAccessFile sReader = new RandomAccessFile(sPath, "r");
		// The number of Strings correspond to the number of positions for the word in S.
		String[] contextArray = new String[offsetsInE.size()];
		
		int index = 0;
		for(long offset : offsetsInE) {
			// Make sure the beginning offset is not negative.
			int beginOffset = (int) Math.max(0, offset - Constants.CONTEXT_SIZE);
			// Make sure the ending offset is not further than the end of file.
			int endOffset = (int) Math.min(sReader.length(), offset + wordLength + Constants.CONTEXT_SIZE);

			// Start reading at the beginning offset.
			sReader.seek(beginOffset);
			
			// Read everything between the beginning and ending offset.
			byte[] byteBuffer = new byte[endOffset - beginOffset]; 
			sReader.readFully(byteBuffer);
			
			// The file is in ISO-8859-1.
			// Replace new lines with spaces as specified by the lab memo.
			contextArray[index] = new String(byteBuffer, "ISO-8859-1").replaceAll("\n", " "); 
			index++;
		}
		sReader.close();
		return contextArray;
	}
	
	/**
	 * 
	 * @param posRange The position range of the sought word in E.
	 * @return The contexts (surrounding characters in S) for the word .
	 * @throws IOException
	 */
	public String[] search(PositionRange posRange) throws IOException {
		ArrayList<Long> positionsInE = climbEverest(posRange);
		String[] wordContexts = getContextArrayFromFile(positionsInE, posRange.word.length());
		return wordContexts;
	}
	
	/**
	 * Reads a line from file and returns the {@link LineData} object corresponding to that line.
	 * 
	 * @param fileInput The file stream to be used to read from.
	 * @return The {@link LineData} object corresponding to that line.
	 * @throws IOException
	 */
	public static LineData readLineDataFromFile(DataInput fileInput) throws IOException {
		String line = fileInput.readLine();
		if (line == null) {
			return null;
		}

		String[] lineData = line.split(" ");
		String word = lineData[0].trim();
		
		return new LineData(word, Long.valueOf(lineData[1]));
	}
	
	/**
	 * Represents the data for a line, including the word and its corresponding position as a long.
	 */
	public static class LineData {
		public final String word;
		public final long position;
		
		public LineData(String word, long position) {
			this.word = word;
			this.position = position;
		}
	}

	
	/**
	 * Contains information about the position range in E for given word. 
	 *
	 */
	public static class PositionRange {
		public final long start, end;
		public final String word;
		public PositionRange(String word, long start, long end) {
			this.word = word;
			this.start = start;
			this.end = end;
		}
		
		/**
		 * @return The number of occurrences of the word in S.
		 */
		public int getOccurrenceCount() {
			if (end <= start) {
				return 0;
			}
			return (int) (end - start) / (Long.SIZE / 8);
		}
	}
	
}
