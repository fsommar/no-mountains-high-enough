package csc.kth.adk14;

import java.util.Scanner;




public class Main {

	public static void main(String[] args) throws Exception {
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
			System.out.print("Enter word to search for: ");
			// TODO: Check for null
			searchTerm = new Scanner(System.in).nextLine().trim();
		} else if (args[0].equals("-g")){
			// generate files
			mountains.generateFromFile();
			// mountains needs to generate its files first
			lazyHash.generateFromFile();
			return;
		} else {
			searchTerm = args[0];
		}
		
		Concordance c = new Concordance(mountains, lazyHash, Constants.S_PATH);
		
		String[] results = c.search(searchTerm);
		System.out.printf("Det finns %d förekomster av ordet '%s'.\n", results.length, searchTerm);
		if (results.length > Constants.MAX_OCCURENCES) {
			System.out.printf("Det finns fler än %d förekomster av ordet '%s'. Vill du visa alla förekomster?\n"+
					"Tryck på enter för att visa eller Ctrl-C för att avbryta.",
					Constants.MAX_OCCURENCES, searchTerm);
			// Listen for input
			new Scanner(System.in).nextLine();
		}
		
		for (String s : results) {
			System.out.println(s);
		}
		
	}
	

}
