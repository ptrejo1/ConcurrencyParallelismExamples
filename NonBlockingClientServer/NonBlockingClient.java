import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.Scanner;

public class NonBlockingClient {
	public static void main(String[] args) {
		new NonBlockingClient.runClient();
	}

	private void runClient() {
		try {
			InetSocketAddress server = new InetSocketAddress(InetAddress.getByName("localhost"), 9876);
			SocketChannel rwChannel = SocketChannel.open();		// communication channel
			rwChannel.configureBlocking(false);		// nonblocking
			rwChannel.connect(server);

			Selector selector = Selector.open();	// monitor
			rwChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			while (true) {
				if (selector.select() > 0) {	// blocking call
					processKeys(selector.selectedKeys());
				}
			}
		} catch (Exception e) { }
	}

	private void processKeys(Set keys) {
		Iterator it = keys.iterator();

		try {
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey) it.next();
				it.remove();

				if (key.isConnectable()) {
					SocketChannel rwChannel = (SocketChannel) key.channel();
					while (rwChannel.isConnectionPending()) {
						rwChannel.finishConnect();
					}
				}

				if (key.isReadable()) {
					String response = getResponse(key);
					System.out.println(response);
				}

				if (key.isWritable()) {
		    		sendRequest(key);
				}
			}
		} catch(Exception e) { }
	}

	private String getResponse(SelectionKey key) {
		Scanner scanner = new Scanner(System.in);

		try {
			SocketChannel rwChannel = (SocketChannel) key.channel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int count = 0;
			String response = "";
			while ((count = rwChannel.read(buffer)) > 0) {	// read until nothing left
				buffer.flip();
				response += Charset.defaultCharset().decode(buffer);
			}
			return response;
		} catch(Exception e) { }

		return "No response";
	}

	private void sendRequest(SelectionKey key) {
		Scanner scanner = new Scanner(System.in);
		try {
			SocketChannel rwChannel = (SocketChannel) key.channel();

			System.out.print("Request to send ('quit' to quit): ");
			String request = scanner.nextLine();
	    	if (request.equalsIgnoreCase("quit")) System.exit(0);

	    	ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
	    	while (buffer.hasRemaining()) rwChannel.write(buffer);
		} catch(Exception e) { }
	}
}