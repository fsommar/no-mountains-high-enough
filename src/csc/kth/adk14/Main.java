package csc.kth.adk14;

import java.io.IOException;
import java.util.Scanner;

import csc.kth.adk14.Concordance.PositionRange;




public class Main {

	public static void main(String[] args) {
		// ask for word to search for
		String searchTerm = "";
		Mountains testMountains = new Mountains(
				Constants.TEST_PATH+"K", 
				Constants.TEST_PATH+"K2", 
				Constants.TEST_PATH+"E");
		LazyHash testLazyHash = new LazyHash(Constants.TEST_PATH+"L", Constants.TEST_PATH+"K2");
		
		Mountains mountains = new Mountains(
				Constants.K_PATH, 
				Constants.K2_PATH, 
				Constants.E_PATH);
		LazyHash lazyHash = new LazyHash(Constants.L_PATH, Constants.K2_PATH);
		

		if (args.length < 1) {
			while (searchTerm.equals("") || searchTerm == null) {
				System.out.print("Mata in ett ord: ");
				searchTerm = new Scanner(System.in).nextLine();
			}
			searchTerm = searchTerm.trim();
		} else if (args[0].equals("-g")){
			try {
				// generate files
				mountains.generateFromFile();
				// mountains needs to generate its files first
				lazyHash.generateFromFile();				
			} catch (IOException e) {
				System.out.println("Misslyckades med att generera filer!\n"+e);
			}
			return;
		} else {
			searchTerm = args[0];
		}
		
		
		Concordance c = null;
		try {
			c = new Concordance(mountains, lazyHash, Constants.S_PATH);			
		} catch (IOException e) {
			System.out.println("Misslyckades med att öppna konkordansen!\n"+e);
		}
		
		try {
			PositionRange posRange = c.searchK2(searchTerm);
			int occurrencesCount = posRange.getOccurrenceCount();
			System.out.printf("Det finns %d förekomster av ordet '%s'.\n", occurrencesCount, searchTerm);
			if (occurrencesCount > Constants.MAX_OCCURENCES) {
				System.out.printf("Det finns fler än %d förekomster av ordet '%s'. Vill du visa alla förekomster?\n"+
						"Tryck på enter för att visa eller Ctrl-C för att avbryta.",
						Constants.MAX_OCCURENCES, searchTerm);
				// Listen for input
				new Scanner(System.in).nextLine();
			}
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
