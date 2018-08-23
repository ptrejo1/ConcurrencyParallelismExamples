/* 
	Threaded server

	#includes 
*/

// 'pure function' doesnt rely on any global info
void* handle_client(void* client_ptr) {		// thread routine
	// pthread_self is id of current thread
	// pthread_detach no other thread is waiting for this thread
	// take back thread resources
	// terminates on return
	pthread_detach(pthread_self());	

	int client = *((int*) client_ptr);	// socket FD

	// request
	char buffer[BUFF_SIZE + 1];
	bzero(buffer, sizeof(response));
	int bytes_read = recv(client, buffer, sizeof(buffer), 0);
	if (bytes_read < 0) error_msg("Problem with recv", false);

	// response
	char response[BUFF_SIZE + 1];
	bzero(response, sizeof(response));
	generate_echo_response(buffer, response);
	int bytes_written = send(client, response, strlen(response), 0);
	if (bytes_written < 0) error_msg("Problem with send", false);

	close(client);
	return NULL; 
	// detached thread terminates in return
}

int main() {
	char buffer[BUFF_SIZE + 1];
	struct sockaddr_in client_addr;
	socklen_t len = sizeof(struct sockaddr_in);
	int sock = create_server_socket(false);	// socket blocks until someone connects

	// Accept connections
	// Main thread accepts client, creates new thread, hands client to new thread
	// main thread goes back to listnening to for new clients
	while (true) {
		int client = accept(sock, (struct sockaddr*) &client_addr, &len);
		if (client < 0) error_msg("Problem accepting request", true);
		announce_client(&client_addr.sin_addr);

		pthread_t tid;
		// thread id, attributes, routine, routine's args
		int flag = pthread_create(&tid, NULL, handle_client, &client);
		if (flag < 0) error_msg("Problm creating thread", false);
	}
	return 0;
}