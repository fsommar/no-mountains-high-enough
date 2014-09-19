package csc.kth.adk14.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import csc.kth.adk14.Concordance;
import csc.kth.adk14.Constants;
import csc.kth.adk14.LazyHash;
import csc.kth.adk14.Mountains;
import csc.kth.adk14.Concordance.PositionRange;

public class EverestTest {
	
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
	public void testFirst() throws IOException {
		PositionRange range = new PositionRange("aldrig", 0, 8);
		ArrayList<Long> expected = new ArrayList<Long>(
				Arrays.asList(311L));
		ArrayList<Long> actual = concordance.climbEverest(range);
		assertArrayEquals(expected.toArray(), actual.toArray());
	}
	
	@Test
	public void testMiddle() throws IOException {
		PositionRange range = new PositionRange("de", 248, 280);
		ArrayList<Long> expected = new ArrayList<Long>(
				Arrays.asList(
						1537L,
						2244L,
						2498L,
						2593L));
		ArrayList<Long> actual = concordance.climbEverest(range);
		assertArrayEquals(expected.toArray(), actual.toArray());
	}
	

	
	@Test
	public void testLast() throws IOException {
		PositionRange range = new PositionRange("över", 3800, 3864);
		ArrayList<Long> expected = new ArrayList<Long>(
				Arrays.asList(
						1026L,
						1304L,
						1387L,
						1603L,
						1690L,
						1726L,
						1930L,
						2011L));
		ArrayList<Long> actual = concordance.climbEverest(range);
		assertArrayEquals(expected.toArray(), actual.toArray());
	}

}
