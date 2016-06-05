package bgu.spl.spl_assignment3;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.spl_assignment3.protocol.ServerProtocol;
//import TBGP.ClientDetails;
/*import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;

import tokenizer.FixedSeparatorMessageTokenizer;
import tokenizer.MessageTokenizer;*/
//import ServerProtocol;
import bgu.spl.spl_assignment3.tokenizer.*;

class MultipleClientProtocolServer implements Runnable {
	private ServerSocket serverSocket;
	private int listenPort;
	private ServerProtocolFactory<TBGPMessage> factory;
	private TokenizerFactory<TBGPMessage> tokenizerFactory; 
	
	Map<String, Room> rooms;
	Map<ProtocolCallback<TBGPMessage>, ClientDetails> clients;
	
	
	public MultipleClientProtocolServer(int port, ServerProtocolFactory<TBGPMessage> p, TokenizerFactory t)
	{
		serverSocket = null;
		listenPort = port;
		factory = p;
		tokenizerFactory = t;
		clients = new ConcurrentHashMap<ProtocolCallback<TBGPMessage>, ClientDetails>();
		rooms = new ConcurrentHashMap<String, Room>();
	}
	
	public void run()
	{
		try {
			serverSocket = new ServerSocket(listenPort);
			System.out.println("Listening...");
		}
		catch (IOException e) {
			System.out.println("Cannot listen on port " + listenPort);
		}

		while (true)
		{
			try {
				
				ServerProtocol<TBGPMessage> s = factory.create(clients, rooms);
				MessageTokenizer<TBGPMessage> a = tokenizerFactory.create();
				ConnectionHandler<TBGPMessage> newConnection = new ConnectionHandler<TBGPMessage>(serverSocket.accept(), s, a);
				new Thread(newConnection).start();
			}
			catch (IOException e)
			{
				System.out.println("Failed to accept on port " + listenPort);
			}
		}
	}
	

	// Closes the connection
	public void close() throws IOException
	{
		serverSocket.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		
		// Get port
		int port = Integer.decode(args[0]).intValue();
		
		MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new TBGPProtocolFactory(), new FixedSeparatorMessageTokenizerFactory());
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Server stopped");
		}
		
		
				
	}
}
