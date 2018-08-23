/** The program picks up the 'scatter/gather' midstream: the book's pages have been
    scattered to eight editors, each of whom submits an ordered list
    of the pages on which a specified word (in this example, 'concurrent') occurs.
    However, the lists of pages come back in arbitrary order, as reflected in the code below.
    The program now illustrates some additional aspects of the 'scatter/gather' pattern.
*/

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Index {
	private Map<String, List<Integer>> index;

	public Index() {
		index = new ConcurrentHashMap<String, List<Integer>>();
	}

	public void addEntry(final String word, final List<Integer> list) {
		List<Integer> tempList = null;
		if (index.containsKey(word)) {		// if already an entry appended
			tempList = index.get(word);
			tempList.addAll(list);
		} else {
			tempList = list;
		}

		tempList.sort((n1, n2) -> n1.compareTo(n2));
		index.put(word, tempList);
	}

	public List<Integer> getEntry(final String word) {
		return index.get(word);
	}
}

class IndexMain  {
	private void log(String msg) {
		System.out.println(msg);
    }

    private List<List<Integer>> init() {
		List<List<Integer>> lol = new ArrayList<List<Integer>>();
		lol.add(new ArrayList<Integer>(Arrays.asList(162,176,187,199)));          // editor 4
		lol.add(new ArrayList<Integer>(Arrays.asList(110,132,133)));              // editor 3
		lol.add(new ArrayList<Integer>(Arrays.asList(4,18,32,43,44,48)));         // editor 1
		lol.add(new ArrayList<Integer>(Arrays.asList(362,376,387,388,399)));      // editor 8
		lol.add(new ArrayList<Integer>(Arrays.asList(54,65,76,89,90)));           // editor 2
		lol.add(new ArrayList<Integer>(Arrays.asList(254,265,276,289,290)));      // editor 6   
		lol.add(new ArrayList<Integer>(Arrays.asList(310,311,332,333,345)));      // editor 7
		lol.add(new ArrayList<Integer>(Arrays.asList(201,203,207,219,227,231)));  // editor 5
		return lol;
    }

    // reduce list of lists into one list
    List<Integer> flatten(final List<List<Integer>> lol) {
    	List<Integer> flatList = 
    		lol
    		.parallelStream()
    		.flatMap(n -> n.stream())		// flatten list of lists into list of ints
    		.collect(Collectors.toList());	// aggregate pieces into single list
    	return flatList
    }

    private void doIt() {
    	List<List<Integer>> lol = init(); 
    	List<Integer> pages = flatten(lol);

    	Index index = new Index();
		index.addEntry("concurrent", pages);
		log("concurrent ==> " + index.getEntry("concurrent"));

		index.addEntry("concurrent", Arrays.asList(401, 402, 417, 435));
		log("\nAfter new addition: " + index.getEntry("concurrent"));
    }

    public static void main(String[ ] args) {
		new IndexMain().doIt();
    }
}