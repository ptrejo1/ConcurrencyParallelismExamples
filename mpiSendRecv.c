/** To compile and run:

    mpicc -o mpiSendRecv mpiSendRecv.c
    mpirun -np N ./mpiSendRecv  ## try values for N such as 2, 4, 8, and 16

    IPC in OpenMPI
    the 'master' process sendsva message to worker1, which sends one to worker2,...,
    which sends a message back to the 'master' process. At this point, the program 
	terminates.
*/

#include <stdio.h>    
#include <string.h>   
#include <mpi.h>

#define MaxLength (64)

void report(char* title, int rank, char digitsIn[ ]) {
	printf("%s %2i received: %s\n", title, rank, digitsIn);
} 

int main() {
	char digitsOut[MaxLength + 1]; /** string terminator **/
	char digitsIn[MaxLength + 1];
	memset(digitsOut, '\0', sizeof(digitsOut));
	memset(digitsIn, '\0', sizeof(digitsIn));

	int rank = -1, size = -1;
	MPI_Status status;

	MPI_Init(0, 0);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if (rank == 0) {
		if (size < 2) puts("\nUsage: mpirun -np N ./mpiBasics for N > 1");

		sprintf(digitsOut, "%i", rank);

		/** Blocking send: function blocks until the specified data arrive at the destination. **/
		MPI_Send(digitsOut,                /** data to send **/
	    	strlen(digitsOut) + 1,    /** count of data items (+ 1 is for the '\0') **/
	    	MPI_CHAR,                 /** data type of each item **/
	    	rank + 1,                 /** destination process by 'rank' (id) **/
	    	1,                        /** user-defined data **/
	    	MPI_COMM_WORLD);          /** built-in communicator **/

		/** Blocking receive: function blocks until the specified number of items are in the buffer. **/
		MPI_Recv(digitsIn,                 /** buffer to receive data **/
	    	MaxLength,                /** buffer size **/
	    	MPI_CHAR,                 /** data type of each buffer element **/
	    	size - 1,                 /** sending process **/
	     	1,                        /** user-defined data **/
	    	MPI_COMM_WORLD,           /** built-in communicator **/
	    	&status);                 /** MPI meta-data **/

		report("Master process", rank, digitsIn);
	}
	// worker
	else {
		MPI_Recv(digitsIn,                      
	    	MaxLength,       /** MaxLength because receiver doesn't know how many are coming. **/                      
	    	MPI_CHAR,                   
	    	MPI_ANY_SOURCE,  /** for generality--don't have to hardcode the source **/          
	    	1,                         
	    	MPI_COMM_WORLD,            
	    	&status);

		report("Worker process", rank, digitsIn);

		sprintf(digitsOut, "%s %i", digitsIn, rank);
		MPI_Send(digitsOut,                    
	    	strlen(digitsOut) + 1,        
	    	MPI_CHAR,                      
	    	(rank + 1) % size,  /** next worker in line, or master if there is none **/
	    	1,                             
	    	MPI_COMM_WORLD);
	}

	MPI_Finalize();
	return 0;
}