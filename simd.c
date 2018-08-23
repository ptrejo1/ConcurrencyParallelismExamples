/* instruction level parallelism */

#include <stdio.h>

#define Length 8
typedef int intV8 __attribute__ ((vector_size (Length * sizeof(int))));

int main() {

	// init vector, no sqaure brackets
	intV8 dataV1 = {1, 2, 3, 4, 5, 6, 7, 8}; 
	intV8 dataV2 = {8, 7, 6, 5, 4, 3, 2, 1};

	// no array syntax
	intV8 add = dataV1 + dataV2;
	intV8 mul = dataV1 * dataV2;
	intV8 div = dataV1 / dataV2;

	int i;
	for (int i; i < Length; i++) {
		printf("%i ", add[i]);		// array syntax
	}
	putchar('\n');
	return 0;
}