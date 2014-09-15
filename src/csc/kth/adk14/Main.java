package csc.kth.adk14;

import java.io.File;
import java.util.Scanner;



public class Main {

	public static void main(String[] args) throws Exception {
		// Generate the L file (remember to run setup.sh before to get the K file)
//		LazyHash.parse(Constants.K_PATH, Constants.L_PATH);
		// Generate the indexArray
//		long[] indexArray = LazyHash.indexArrfromL(Constants.L_PATH, Constants.K_PATH);
		
		// ask for word to search for
		System.out.println("Enter word to search for: ");
		//String input = new Scanner(System.in).nextLine();
		for (byte b : "åäö".getBytes())
			System.out.printf("%X ", b);
		
	}
	
	/*
	 * Use the searchK method together with a RandomAccessFile to the S file to get surrounding text.
	 * Extract methods into a class which keeps track of the index array and L, K and S files.
	 * 
	 */

}
