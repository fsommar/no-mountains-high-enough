package csc.kth.adk14.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import csc.kth.adk14.Concordance;
import csc.kth.adk14.Constants;
import csc.kth.adk14.LazyHash;
import csc.kth.adk14.Mountains;
import csc.kth.adk14.WordNotFoundException;

public class SearchTest {

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
	public void testFirstInSource() throws IOException, WordNotFoundException {
		String word = "hej";
		String[] actual = concordance.search(concordance.searchK2(word));
		
		String[] expected = new String[] {"Hej! När vi sverigedemokrater säg"};
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testMiddleInSource() throws IOException, WordNotFoundException {
		String word = "men";
		String[] actual = concordance.search(concordance.searchK2(word));
		
		String[] expected = new String[] {"a banor och vågar göra tuffa, men nödvändiga prioriteringar.  N"};
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testLastInSource() throws IOException, WordNotFoundException {
		String word = "låtsas";
		String[] actual = concordance.search(concordance.searchK2(word));
		
		// File has a trailing new line that doesn't want to go away..
		String[] expected = new String[] {" bor här. På riktigt. Fast på låtsas. "};
		assertArrayEquals(expected, actual);
	}
	
	@Test(expected=WordNotFoundException.class)
	public void testLastHash() throws IOException, WordNotFoundException {
		String word = "öööh";
		
		String[] invalid = concordance.search(concordance.searchK2(word));
	}
	
	@Test(expected=WordNotFoundException.class)
	public void testFirstHash() throws IOException, WordNotFoundException {
		String word = "   ";
		
		String[] invalid = concordance.search(concordance.searchK2(word));
	}
	
	
	@Test(expected=WordNotFoundException.class)
	public void testAAAHash() throws IOException, WordNotFoundException {
		String word = "aaa";
		
		String[] invalid = concordance.search(concordance.searchK2(word));
	}
	
	@Test
	public void testDifferentCase() throws IOException, WordNotFoundException {
		String word = "YTTRANDEFRIHET";
		String[] actual = concordance.search(concordance.searchK2(word));
		
		String[] expected = new String[] {"are. Vi är tacksamma över vår yttrandefrihet, vår jämställdhet och vårt st"};
		assertArrayEquals(expected, actual);
	}
	
	@Test
	public void testMultipleResults() throws IOException, WordNotFoundException {
		String word = "älskar";
		String[] actual = concordance.search(concordance.searchK2(word));
		
		String[] expected = new String[] {
				"är vi väldigt stolta över. Vi älskar den svenska naturen med alla ",
				" land ännu bättre. För att vi älskar Sverige och människorna som b",
				"verigedemokrater säger att vi älskar Sverige så menar vi det. På r"
		};
		assertArrayEquals(expected, actual);
	}

}
