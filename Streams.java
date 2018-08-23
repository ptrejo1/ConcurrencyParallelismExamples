/* map reduce example */

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class Streams {
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < 16; i++) list.add(i);

		// ex 1
		list
			.stream()		// iteraton construct, only use once
			.map(n -> n+1)	
			.forEach(System.out::println);
		System.out.println();

		// ex 2
		list
			.stream()
			.map(n -> Arrays.asList(n-1, n, n+1))	// output is array
			.forEach(System.out::println);
		System.out.println();

		// ex 3
		list
			.stream()
			.map(n -> n+1)
			.forEach(n -> System.out.format("%d %s\n", n, Thread.currentThread().getName()));
		System.out.println();

		// ex 4
		list
			.parallelStream()
			.map(n -> n+1)
			.forEach(n -> System.out.format("%d %s\n", n, Thread.currentThread().getName()));
		System.out.println();

		// ex 5
		int intSuum = 
			list
				.parallelStream()	// needed for reduce
				.map(n -> n+1)
				// 1st arg is identity, serves as default if no more info
				// 2nd accumulator 
				// 3rd is combiner
				.reduce(0, (sum, n) -> sum += n, (sum1, sum2) -> sum1 + sum2);
		System.out.println("\n### The sum: " + intSum);
		System.out.println();

		// ex 6
		intSum = 
			list
				.parallelStream()
				.map(n -> n+1)
				.reduce(0,
					(sum, n) -> { System.out.format("Accumulte: n (%d) and sum (%d)\n", n, sum);
						return sum += n; }, 
					(sum1, sum2) -> { System.out.format("Combine: sum1 (%d) and sum2 (%d)\n", sum1, sum2);
						return sum1 + sum2; });
	}
}
