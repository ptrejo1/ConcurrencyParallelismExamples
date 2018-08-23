/*  A polling server: concurrency through non-blocking I/O 

# Division of labor for the sockets:
 
   ## There's one 'listening' socket through which clients connect.
   
   ## Per client, there's a read/write socket through which the client sends the
      request bytes, and the server sends the response bytes.

 # Basic server structure -- the trickiest so far

      while (true)          ## Run the server indefinitely: kill with Control-C
         for (...)          ## Iterate through the pending connection and request events
	   		if (...)         ## connection events
	     		while (...)    ## could be indefinitely many connections at the same time
	   		else             ## request events: read request, write response  

   ## Enter a infinite loop to await client connections and client requests: the two are
      handled separately.

   ## Wait for notification of relevant events (connections and requests) from the epoll library.
      ### The 'wait' call is the only blocking one in the code.

   ## Determine (in an if-else construct) whether an incomming event is a connection or request:
      ### The if-clause handles connections.
      ### The else-clause handles HTTP requests (and generates HTTP responses).

   ## There could be one or more connection events at any time; hence, a 'while' loop is used
      to handle all of them in the if-clause:
      ### For each connection, register the request/response-socket with the epoll library.

   ## For each request/response event, read the request and then generate an 'echo' response.
      ### The implementation makes a critical, simplifying assumption for convenience: all of the
	  request bytes can be read in a single 'read' operation.

*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <netinet/in.h>
#include <signal.h>
#include <sys/epoll.h>
#include <fcntl.h>
#include <errno.h>
#include "utils.h"

#define MAX_BUFFERS (BACKLOG + 1)

int main() {

	// setup networking
	char buffer[BUFF_SIZE + 1];
	struct sockaddr_in client_addr;
	socklen_t len = sizeof(struct sockaddr_in);
	int sock = create_server_socket(true);		// nonblocking socket

	struct epoll_event event, 		// server2epoll
		event_buffers[MAX_BUFFERS];		// epoll2server
	int epollfd = epoll_create1(0);
	if (epollfd < 0) error_msg("epoll", true);

	// Register listening-for-clients event
	event.events = EPOLLIN;		// incoming events
	event.data.fd = sock;		// fd to monitor
	if (epoll_ctl(epollfd, EPOLL_CTL_ADD, sock, &event) < 0)	// setup
		error_msg("epoll_ctl", true);

	// Await conns + requests
	while (true) {
		int n = epoll_wait(epollfd, event_buffers, MAX_BUFFERS, -1);	// blocking wait call, get # of events
		if (n < 0) error_msg("epoll_wait", true);

		/*
       -- If connection, register for request events: may be none or more connections
       -- If request, read and echo 
        */
		int i;
		for (i = 0; i < n; i++) {
			// conns
			if (event_buffers[i].data.fd == sock) {
				while (true) {
					socklen_t len = sizeof(client_addr);
					int client = accept(sock, (struct sockaddr*) &client_addr, &len);

					// no client
					if (client < 0 && (EAGAIN == errno || EWOULDBLOCK == errno)) 
						break;

					// client
					fcntl(client, F_SETFL, O_NONBLOCK);		// rw socket nonblocking
					event.events = EPOLLIN;					// incoming events
					event.data.fd = client;					// assign fd to socket
					if (epoll_ctl(epollfd, EPOLL_CTL_ADD, client, &event) < 0)	// tell library
	    				error_msg("Problem with epoll_ctl ADD call", false);
					announce_client(&client_addr.sin_addr);
				}
			}
			//  requests
			else {
				bzero(buffer, sizeof(buffer));
				int bytes_read = recv(event_buffers[i].data.fd, buffer, sizeof(buffer), 0);

				// echo request
				if (bytes_read > 0) {
					char response[BUFF_SIZE * 2];
					bzero(response, sizeof(response));
					generate_echo_response(buffer, response);
					int bytes_written = send(event_buffers[i].data.fd, response, strlen(response), 0);
					if (bytes_written < 0) 
						error_msg("send call", false);
					close(event_buffers[i].data.fd);	// epoll stops polling fd
				}
			}
		}
	}

	return 0;
} 