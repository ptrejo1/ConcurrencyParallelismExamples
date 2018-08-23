/*
Child does read write operations
Parent awaits client connections
Spawning processes

#includes are here
*/

int main()
{
	// avoid zombies
	// tells os we dont care about receiving signals
	// about child processes terminating
	signal(SIGCHLD, SIG_IGN);	

	char buffer[BUFF_SIZE+1];
	struct sockaddr_in client_addr;
	socklen_t len = sizeof(struct sockaddr_in);
	int sock = create_server_socket(false);		// get fd for regular blocking socket

	while (true) {
		int client = accept(sock, (struct sockaddr*) &client_addr, &len);
		if (client < 0) error_msg("Problem, with accept call", true);
		announce_client(&client_addr.sin_addr);

		/* fork child */
		pid_t pid = fork();
		if (pid < 0) error_msg("Problem with fork", false);

		/* child */
		if (pid == 0) {
			close(sock);	// close listening socket

			bzero(buffer, sizeof(buffer));	// place zeros in buffer
			int bytes_read = recv(client, buffer, sizeof(buffer), 0);	// read client request
			if (bytes_read < 0) error_msg("Problem with recv call", false);

			char response[BUFF_SIZE*2];
			bzero(response, sizeof(response));
			generate_echo_response(buffer, response);
			int bytes_written = send(client, response, strlen(response), 0);	// send response
			if (bytes_written < 0) error_msg("Problen with send call", false);

			close(client);	// close client
			exit(0);
		} else {
			close(client);	// close read write socket
		}
	}
	return 0;
}