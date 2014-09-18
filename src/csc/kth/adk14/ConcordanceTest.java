package csc.kth.adk14;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import csc.kth.adk14.Concordance.PositionRange;

public class ConcordanceTest {

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
	public void testMiddle() throws IOException {
		PositionRange range = concordance.searchK2("demokrati");
		assertEquals(296, range.start);
		assertEquals(304, range.end);
	}
	
	@Test
	public void testFirst() throws IOException {
		PositionRange range = concordance.searchK2("aldrig");
		assertEquals(0, range.start);
		assertEquals(8, range.end);
	}
	
	@Test
	public void testLast() throws IOException {
		PositionRange range = concordance.searchK2("över");
		assertEquals(3800, range.start);
		assertEquals(mountains.getEverest().length(), range.end);
	}
	
	public static class ClimbEverest {
		
		@Test
		public void testFirst() throws IOException {
			PositionRange range = new PositionRange(0, 8);
			ArrayList<Long> expected = new ArrayList<Long>(
					Arrays.asList(307L));
			ArrayList<Long> actual = concordance.climbEverest(range);
			assertArrayEquals(expected.toArray(), actual.toArray());
		}
		
		@Test
		public void testMiddle() throws IOException {
			PositionRange range = new PositionRange(248, 280);
			ArrayList<Long> expected = new ArrayList<Long>(
					Arrays.asList(
							1533L,
							2240L,
							2494L,
							2589L));
			ArrayList<Long> actual = concordance.climbEverest(range);
			assertArrayEquals(expected.toArray(), actual.toArray());
		}
		

		
		@Test
		public void testLast() throws IOException {
			PositionRange range = new PositionRange(3768, 3832);
			ArrayList<Long> expected = new ArrayList<Long>(
					Arrays.asList(
							1022L,
							1300L,
							1383L,
							1599L,
							1686L,
							1722L,
							1926L,
							2007L));
			ArrayList<Long> actual = concordance.climbEverest(range);
			assertArrayEquals(expected.toArray(), actual.toArray());
		}
	}
	
	public static class GetContextArray {
		
		@Test
		public void testFirst() throws IOException {
			String word = "hej";
			String[] actual = concordance.search(word);
			
			String[] expected = new String[] {"Hej! När vi sverigedemokrater säg"};
			assertArrayEquals(expected, actual);
		}
		
		@Test
		public void testMiddle() throws IOException {
			String word = "men";
			String[] actual = concordance.search(word);
			
			String[] expected = new String[] {"a banor och vågar göra tuffa, men nödvändiga prioriteringar.  N"};
			assertArrayEquals(expected, actual);
		}
		
		@Test
		public void testLast() throws IOException {
			String word = "låtsas";
			String[] actual = concordance.search(word);
			
			// File has a trailing new line that doesn't want to go away..
			String[] expected = new String[] {" bor här. På riktigt. Fast på låtsas. "};
			assertArrayEquals(expected, actual);
		}
	}
	


}
