all: K compile mountains

# skapa konkordansen (K file) m.h.a. tokenizer
K: 
	./setup.sh

# kompilera projektet
compile:  
	javac -cp src src/csc/kth/adk14/*.java

# skapa K2, Everest och LazyHash.
mountains:
	java -cp src csc.kth.adk14.Main -g

clean:
	rm src/csc/kth/adk14/*.class
	rm /var/tmp/L
	rm /var/tmp/K
	rm /var/tmp/K2
	rm /var/tmp/E

