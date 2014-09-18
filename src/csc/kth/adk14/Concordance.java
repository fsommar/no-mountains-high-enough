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
		
		lazyHash.readIndexArrFromFile();
	}

	/**
	 * Use binary search to find a word and its corresponding range of positions in a {@link PositionRange} in the K2 file.
	 * @return the position of the word in the S file, or -1 if the word wasn't found.
	 */
	public PositionRange searchK2(String searchTerm)
			throws IOException {
		// Find byte offset of the word in K through L
		int hash = LazyHash.hash(searchTerm);
		searchTerm = searchTerm.toLowerCase();
		
		long[] indexArray = lazyHash.getIndexArray();
		
		long startRange = indexArray[hash];
		long endRange = indexArray[hash+1];
		
		// Init a reader
		RandomAccessFile k2Reader = new RandomAccessFile(mountains.getK2(), "r");
		
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
			k2Reader.readLine();
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
					System.err.println("went too far in searchK2: "+data.word);
					break;
				}
			}
			return null;
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
		eReader.seek(range.start);
		ArrayList<Long> results = new ArrayList<Long>();
		
		while (eReader.getFilePointer() < range.end) {
			results.add(eReader.readLong());
		}
		return results;
	}
	
	/**
	 * Gets the the search term from the source file including the surrounding context.
	 * 
	 * @param offsetsInE
	 * @param wordLength the length of the search term in bytes.
	 * @return
	 * @throws IOException
	 */
	public String[] getContextArrayFromFile(ArrayList<Long> offsetsInE, int wordLength) throws IOException {
		RandomAccessFile sFile = new RandomAccessFile(sPath, "r");
		String[] contextArray = new String[offsetsInE.size()];
		int index = 0;
		for(long offset : offsetsInE) {
			int beginOffset = (int) Math.max(0, offset - Constants.CONTEXT_SIZE);
			int endOffset = (int) Math.min(sFile.length(), offset + wordLength + Constants.CONTEXT_SIZE);

			sFile.seek(beginOffset);
			
			byte[] byteBuffer = new byte[endOffset - beginOffset]; 
			sFile.readFully(byteBuffer);
			
			contextArray[index] = new String(byteBuffer, "ISO-8859-1").replaceAll("\n", " "); 
			index++;
		}
		
		return contextArray;
	}
	
	public String[] search(String word) {
		try {
			PositionRange pr = searchK2(word);
			ArrayList<Long> al = climbEverest(pr);
			String[] actual = getContextArrayFromFile(al, word.length());
			return actual;
		} catch (IOException e) {
			System.err.println("concordance.search: "+e);
			return null;
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
