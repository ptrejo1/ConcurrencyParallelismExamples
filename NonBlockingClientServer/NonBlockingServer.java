/*
	To compile and run the application:

    javac NonBlockingServer.java
    javac NonBlockingClient.java
    java NonBlockingServer
    java NonBlockingClient ## using a separate command-line prompt

    Server can be terminated with Control-C.
*/

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingServer {
	public static void main(String[] args) {
		new NonBlockingServer().runServer();
	}

	private void runServer() {
		try {
			final int port = 9876;
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);		// set for nonblocking
			serverSocket.socket().bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));

			System.out.println("Server accepting connections on port " + port);

			// Await connections
			Selector selector = Selector.open();	// monitor overlooks connection and requests
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);	// accept conns on channel
			while (true) {
				if (selector.select() <= 0) continue;	// blocking call, wait for client conns
				acceptConnections(selector.SelectionKeys());	// conns have identfier called a key
			}

		} catch(Exception e) { } 
	}

	private void acceptConnections(Set keys) {
		try {
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();	// get key associated w/ particular conn
				it.remove();									// remove key from keys

				if (key.isAcceptable()) {		// accept 
					ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
					SocketChannel rwChannel = (SocketChannel) serverSocket.accept(); // get read write channel
					rwChannel.configureBlocking(false);		// configure nonblocking
					rwChannel.register(key.selector(), SelectionKey.OP_READ);	// register key for read ops
				}

				if (key.isReadable()) {		// client has sent request that needs to be read
					String request = handleRead(key);

					if (request.length() > 0) {
						SocketChannel rwChannel = (SocketChannel) key.channel();	// get rw channel
						request = "Echoing: " + request;
						ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());	// bytes for networking
						rwChannel.write(buffer);							// nonblocking
						System.out.println(new String(buffer.array()));
					}
				}
			}
		} catch(Exception e) { }  
	}

	private String handleRead(SelectionKey key) {
		try {
			SocketChannel rwChannel = (SocketChannel) key.channel();	// get channel
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int bytesCount = rwChannel.read(buffer);		// nonbocking
			if (bytesCount > 0) {
				buffer.flip();
				return new String(buffer.array());		// return buffer 
			}

		} catch(Exception e) { } 
		return "No request";
	}
}