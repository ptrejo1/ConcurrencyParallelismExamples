/* Divides jobs into chunks, each asigned to worker
	OpenMP handles distribution and processing of chunks */

#include <omp.h>
#include <stdio.h>
#include <stdlib.h>

#define JobSize (100)
#define ChunkSize (10)

int main() {
	int thread_count, tid, i, chunk = ChunkSize;
	float a[JobSize], b[JobSize], c[JobSize];

	for (i = 0; i < JobSize; i++) a[i] = b[i] = i * 1.1f;

	#pragma omp parallel shared(a, b, c, thread_count, chunk) private(i, tid)
	{
		tid = omp_get_thread_num();
		if (tid == 0) {
			thread_count = omp_get_num_threads();
			printf("Thread count %i\n", thread_count);
		}
		printf("worker thread %i \n", tid);

		// dynamic scheduling on chunks
		#pragma omp for schedule(dynamic, chunk)
		for (i = 0; i < JobSize; i++) {
			c[i] = a[i] + b[i];
			printf("worker %i c %i == f %i\n", tid, i, c[i]);
		}
	}
	puts("single thread again")
	for (i = 0; i < JobSize; i++) printf("c %i == f %i\n", i, c[i]);

	return 0;
}