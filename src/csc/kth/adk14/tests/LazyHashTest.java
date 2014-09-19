package csc.kth.adk14;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import csc.kth.adk14.Concordance.PositionRange;

public class LazyHashTest {
	
	public static class HashCode {

		@Test
		public void testNormal() {
			assertEquals(900*1 + 30*1 + 1, LazyHash.hash("aaa"));
		}
		
		@Test
		public void testOneChar() {
			assertEquals(1, LazyHash.hash("a"));
		}
		
		@Test
		public void testPositions() {
			assertEquals(900*1, LazyHash.hash("a  "));
			assertEquals(30*1, LazyHash.hash(" a "));
			assertEquals(1, LazyHash.hash("  a"));
		}
		
		@Test
		public void testEnglishAlphabet() {
			char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
			for (int i = 0; i < alphabet.length; i++) {
				int val = i+1;
				assertEquals(val, LazyHash.hash(String.valueOf(alphabet[i])));				
			}
		}
		
		@Test
		public void testNoChars() {
			assertEquals(0, LazyHash.hash(""));
		}
		
		@Test
		public void testSwedishChars() {
			assertEquals(29, LazyHash.hash("ö"));
			assertEquals(28, LazyHash.hash("ä"));
			assertEquals(27, LazyHash.hash("å"));
		}
		
		@Test
		public void testLongString() {
			assertEquals(900*12 + 30*15 + 14, LazyHash.hash("longstring"));
		}
		
		@Test
		public void testCaps() {
			assertEquals(900*3 + 30*1 + 16, LazyHash.hash("CAP"));
		}
		
		@Test
		public void testSpaces() {
			assertEquals(0, LazyHash.hash("   "));
			assertEquals(0, LazyHash.hash("  "));
			assertEquals(0, LazyHash.hash(" "));
		}
	}
	
	public static class IndexArray {
		static long[] indexArray;
		static File k2File;
		
		@BeforeClass
		public static void setup() throws Exception {
			k2File = new File(Constants.TEST_PATH+"K2");
			Mountains cn = new Mountains(Constants.TEST_PATH+"K", k2File.getPath() , Constants.TEST_PATH+"E");
			cn.generateFromFile();
			
			LazyHash lh = new LazyHash(Constants.TEST_PATH+"L", Constants.TEST_PATH+"K2");
			lh.generateFromFile();
			
			indexArray = lh.readIndexArrFromFile();	
		}
		
		@Test
		public void testFirst() {
			assertEquals(0, indexArray[LazyHash.hash("aldrig")]);
			assertEquals(0, indexArray[0]);
		}
		
		@Test
		public void testSecond() {
			assertEquals(9, indexArray[LazyHash.hash("alla")]);
		}
		
		@Test
		public void testDemocracy() {
			assertEquals(132, indexArray[LazyHash.hash("demokrati")]);
		}
		
		@Test
		public void testLastActual() {
			assertEquals(2806, indexArray[LazyHash.hash("över")]);			
		}
		
		@Test
		public void testEndOfFile() {
			assertEquals(k2File.length(), indexArray[indexArray.length-1]);
		}
	
	}
}
