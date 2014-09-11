package csc.kth.adk14;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		long[] indexArray;
		File abc;
		
		private void generateTestCases(String testCases, String output) throws Exception {
			BufferedReader testCasesReader = new BufferedReader(new FileReader(new File(testCases)));
			DataOutputStream outputWriter = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(output)));
			  
			String line;

			while ((line = testCasesReader.readLine()) != null) {
				String[] data = line.split(" "); 
				
				outputWriter.writeUTF(data[0]);
				outputWriter.writeLong(Integer.valueOf(data[1].trim()));
			}
			testCasesReader.close();
			outputWriter.close();
		}
		
		private void printArray() {
			for (int i = 0; i < 100; i++) {
				System.out.println(i + ": " + indexArray[i]);
			}
		}
		
		@Before
		public void setup() throws Exception {
			String PATH = "/afs/nada.kth.se/home/i/u1k3g18i/projects/adk14/test_indexarray.txt";
			String output = "/afs/nada.kth.se/home/i/u1k3g18i/projects/adk14/abc.dat";
			abc = new File(output);
			generateTestCases(PATH, output);
			indexArray = LazyHash.indexArrfromL(output, PATH);	
//			printArray();
		}
		
		@After
		public void teardown() {
			indexArray = null;
			abc = null;
		}
		
		@Test
		public void testFirst() {
			assertEquals(1337, indexArray[0]);
			assertEquals(1337, indexArray[LazyHash.hash("a")]);
		}
		
		@Test
		public void testLastActual() {
			assertEquals(29, indexArray[LazyHash.hash("ödla")]);			
		}
		
		@Test
		public void testEndOfFile() {
			assertEquals(abc.length(), indexArray[indexArray.length-1]);
		}
	
	}
}
