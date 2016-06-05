package bgu.spl.spl_assignment3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import bgu.spl.spl_assignment3.protocol.ServerProtocol;
import bgu.spl.spl_assignment3.tokenizer.*;

class ConnectionHandler<P> implements Runnable, ProtocolCallback<P> {
	
	private BufferedReader in;
	private PrintWriter out;
	Socket clientSocket;
	ServerProtocol<P> protocol;
	MessageTokenizer<P> tokenizer;
	private static final int READ_BUFFER_SIZE = 100;
	
	public ConnectionHandler(Socket acceptedSocket, ServerProtocol<P> p, MessageTokenizer<P> t)
	{
		in = null;
		out = null;
		clientSocket = acceptedSocket;
		protocol = p;
		tokenizer = t;
		System.out.println("Accepted connection from client!");
		System.out.println("The client is from: " + acceptedSocket.getInetAddress() + ":" + acceptedSocket.getPort());
	}
	
	public void run()
	{		
		try {
			initialize();
		}
		catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}

		try {
			process();
		} 
		catch (IOException e) {
			System.out.println("Error in I/O");
		} 
		
		System.out.println("Connection closed - bye bye...");
		close();

	}
	
	public void process() throws IOException
	{
		P protocolMsg;
		String msg; 
		//CharsetEncoder encoder = Charset.forName("UTF-16").newEncoder();
		while ((msg = in.readLine()) != null)
		{
			System.out.println("Received from client. Num bytes: " + msg.length());
			tokenizer.addBytes(ByteBuffer.wrap(msg.concat("\n").getBytes()));
			
			if(tokenizer.hasMessage())
			{
				protocolMsg = tokenizer.nextMessage();
				
				protocol.processMessage(protocolMsg, this);
				
				if (protocol.isEnd(protocolMsg))
				{
					break;
				}
			}
		}
	}
	
	// Starts listening
	public void initialize() throws IOException
	{
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8"), true);
		System.out.println("I/O initialized");
	}
	
	// Closes the connection
	public void close()
	{
		try {
			if (in != null)
			{
				in.close();
			}
			if (out != null)
			{
				out.close();
			}
			
			clientSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("Exception in closing I/O");
		}
	}

	@Override
	public void sendMessage(P msg) throws IOException {
		out.println(msg.toString());
		System.out.println("Sending message " + msg.toString());
	}
	
}
