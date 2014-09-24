package csc.kth.adk14;

import java.io.IOException;
import java.util.Scanner;

import csc.kth.adk14.Concordance.PositionRange;




/**
 * Given a search term, finds the context in which the term appears in given source file.
 * 
 * We'll be working with five (5) files:
 * 	S - the source file. 															(ISO-8859-1)
 *  K - the generated concordance (word : byte offset in S). 						(ISO-8859-1)
 *  K2 - distinct words with position of the first occurrence in E. 				(ISO-8859-1)
 *  E - (a.k.a. Everest) a list of byte offsets in S sorted by word. 				(binary)
 *  L - the lazyhash lookup file (hash of first three chars : byte offset in K2). 	(binary)
 *  
 * @author fsommar, miloszw
 *
 */
public class Main {

	public static void main(String[] args) {
		// Instantiate file initiatiors.
		
		// Test cases.
		Mountains testMountains = new Mountains(
				Constants.TEST_PATH+"K", 
				Constants.TEST_PATH+"K2", 
				Constants.TEST_PATH+"E");
		LazyHash testLazyHash = new LazyHash(Constants.TEST_PATH+"L", Constants.TEST_PATH+"K2");
		
		// Real deal.
		Mountains mountains = new Mountains(
				Constants.K_PATH, 
				Constants.K2_PATH, 
				Constants.E_PATH);
		LazyHash lazyHash = new LazyHash(Constants.L_PATH, Constants.K2_PATH);
		
		// Ask user for input.
		String searchTerm = "";
		if (args.length < 1) {
			// Loop while input is invalid
			while (searchTerm.equals("") || searchTerm == null) {
				System.out.print("Mata in ett ord: ");
				searchTerm = new Scanner(System.in).nextLine();
			}
			// Trim whitespace and new line from searchTerm
			searchTerm = searchTerm.trim();
		} else if (args[0].equals("-g")){
			// check for the generate-flag
			try {
				// generate files
				mountains.generateFromFile();
				// mountains needs to generate its files first
				lazyHash.generateFromFile();				
			} catch (IOException e) {
				System.out.println("Misslyckades med att generera filer!\n"+e);
			}
			// only generate files, terminate early
			return;
		} else {
			// if search term is supplied as argument, use it
			searchTerm = args[0];
		}
		
		long startTime = System.currentTimeMillis();
		// Init concordance
		Concordance c = null;
		try {
			c = new Concordance(mountains, lazyHash, Constants.S_PATH);			
		} catch (IOException e) {
			System.out.println("Misslyckades med att öppna konkordansen!\n"+e);
			// unrecoverable, quit
			return;
		}
		
		try {
			// Get position range for the given search term in the Everest file.
			PositionRange posRange = c.searchK2(searchTerm);
			
			// Calc the number of occurrences of the search term in the source file from the position range.
			int occurrencesCount = posRange.getOccurrenceCount();
			System.out.printf("Det finns %d förekomster av ordet '%s'.\n", occurrencesCount, searchTerm);
			System.out.printf("Det tog %.3f sekunder.\n", (System.currentTimeMillis()-startTime)/1000.0);
			// If occurrence count is above threshold, confirm if user wants to print all occurrences. 
			if (occurrencesCount > Constants.MAX_OCCURENCES) {
				System.out.printf("Det finns fler än %d förekomster av ordet '%s'. Vill du visa alla förekomster?\n"+
						"Tryck på enter för att visa eller Ctrl-C för att avbryta.",
						Constants.MAX_OCCURENCES, searchTerm);
				
				// Listen for input. User will terminate program if needed, otherwise continue
				new Scanner(System.in).nextLine();
			}
			
			// Get all the occurrences and display them 
			String[] results = c.search(posRange);
			for (String s : results) {
				System.out.println(s);
			}
			
		} catch (WordNotFoundException e) {
			System.out.println("Ordet du söker finns inte! :(");
		} catch (IOException e) {
			System.out.println("Filen finns inte!"+e);
		}
	}
	

}
