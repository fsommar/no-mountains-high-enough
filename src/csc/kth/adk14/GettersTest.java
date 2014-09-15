package csc.kth.adk14;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

public class GettersTest {
	
	static long[] indexArray;

	@Test
	public void testFirstWord() {
		//assertEquals(1,Getters.searchL("anka", indexArray));
	}
	
	@BeforeClass
	public static void setup() throws Exception {
		String input = "/afs/nada.kth.se/home/i/u1k3g18i/projects/adk14/test_indexarray.txt";
		String output = "/afs/nada.kth.se/home/i/u1k3g18i/projects/adk14/abc.dat";
//		LazyHash lh = new LazyHash()
//		LazyHash.parse(input, output);
//		indexArray = LazyHash.indexArrfromL(output, input);	
	}
	
	@Test
	public void testTest() throws Exception {
//		System.out.println(new File(Constants.TEST_ABC_123_456).length());
//		long res = Getters.searchK("ödla", indexArray, Constants.TEST_INDEXARRAY);
//		System.out.println(res);
	}

}
