package csc.kth.adk14.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import csc.kth.adk14.Concordance;
import csc.kth.adk14.Concordance.PositionRange;
import csc.kth.adk14.Constants;
import csc.kth.adk14.LazyHash;
import csc.kth.adk14.Mountains;
import csc.kth.adk14.WordNotFoundException;

public class K2Test {

	static long[] indexArray;
	static File k2File;
	static Concordance concordance;
	static Mountains mountains;
	
	@BeforeClass
	public static void setup() throws Exception {
		k2File = new File(Constants.TEST_PATH+"K2");
		mountains = new Mountains(Constants.TEST_PATH+"K", k2File.getPath() , Constants.TEST_PATH+"E");
		mountains.generateFromFile();
		
		LazyHash lh = new LazyHash(Constants.TEST_PATH+"L", Constants.TEST_PATH+"K2");
		lh.generateFromFile();
		
		concordance = new Concordance(mountains, lh, Constants.TEST_PATH+"S");
		
		indexArray = lh.readIndexArrFromFile();	
	}
	
	@Test
	public void testFirstInK2() throws IOException, WordNotFoundException {
		PositionRange range = concordance.searchK2("aldrig");
		assertEquals(0, range.start);
		assertEquals(8, range.end);
	}
	
	@Test
	public void testMiddleInK2() throws IOException, WordNotFoundException {
		PositionRange range = concordance.searchK2("demokrati");
		assertEquals(296, range.start);
		assertEquals(304, range.end);
	}
	
	@Test
	public void testLastInK2() throws IOException, WordNotFoundException {
		PositionRange range = concordance.searchK2("över");
		assertEquals(3800, range.start);
		// End range is at end of file
		assertEquals(mountains.getEverest().length(), range.end);
	}
}
