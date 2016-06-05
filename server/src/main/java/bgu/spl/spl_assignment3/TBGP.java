package bgu.spl.spl_assignment3;
import java.io.IOException;
import java.util.Map;

import bgu.spl.spl_assignment3.protocol.AsyncServerProtocol;
import bgu.spl.spl_assignment3.tokenizer.*;

class TBGPProtocolFactory implements ServerProtocolFactory<TBGPMessage> {
	
	@Override
	public TBGP create(Map<ProtocolCallback<TBGPMessage>, ClientDetails> clients, Map<String, Room> rooms) {
		return new TBGP(clients, rooms);
	}
}

public class TBGP implements AsyncServerProtocol<TBGPMessage> {
	boolean bShouldClose;
	Map<String, Room> rooms;
	/*private class ClientDetails{
		//public ProtocolCallback<TBGPMessage> callback;
		public String roomName;
		public String nickName;
	}*/
	Map<ProtocolCallback<TBGPMessage>, ClientDetails> clients;
	
	public TBGP(Map<ProtocolCallback<TBGPMessage>, ClientDetails> clients, Map<String, Room> rooms) {
		this.clients = clients;
		this.rooms = rooms;
		bShouldClose = false;
	}
	
	@Override
	public void processMessage(TBGPMessage msg, ProtocolCallback<TBGPMessage> callback) {
		try{
			switch(msg.getCommand()){
			case "NICK":
			{
				if(clients.containsKey(callback)){
					// User already has a nick
					callback.sendMessage(new TBGPMessage("SYSMSG NICK REJECTED"));
				}else{
					boolean isNickInUse = false;
					for(ClientDetails clientDetails : clients.values()){
						if(clientDetails.nickName.equals(msg.getCommandParams().getFirst())){
							isNickInUse = true;
							break;
						}
					}
					if(isNickInUse){
						callback.sendMessage(new TBGPMessage("SYSMSG NICK REJECTED"));
					}
					else
					{
						ClientDetails details = new ClientDetails();
						details.nickName = msg.getCommandParams().getFirst();
						details.roomName = "";
						clients.put(callback, details);
						callback.sendMessage(new TBGPMessage("SYSMSG NICK ACCEPTED"));	
					}	
	
				}
								
				break;
			}
			case "JOIN":
			{
				if(rooms.containsKey(msg.getCommandParams().getFirst()) && rooms.get(msg.getCommandParams().getFirst()).IsGameRunning()){
					callback.sendMessage(new TBGPMessage("SYSMSG JOIN REJECTED"));
				}else if(clients.containsKey(callback)){
					boolean bCannotJoin = false;
					for(Room room : rooms.values()){
						if(room.getPlayers().containsKey(callback)){
							//bIsUserAlreadyJoined = true;
							if(room.IsGameRunning()){
								bCannotJoin = true;
							}else{
								room.removePlayer(callback);
							}
							
							break;
						}
					}
					
					if(bCannotJoin){
						callback.sendMessage(new TBGPMessage("SYSMSG JOIN REJECTED"));
					}else if(rooms.containsKey(msg.getCommandParams().getFirst())){
						if(rooms.get(msg.getCommandParams().getFirst()).IsGameRunning()){
							callback.sendMessage(new TBGPMessage("SYSMSG JOIN REJECTED"));
						}
						else{
							rooms.get(msg.getCommandParams().getFirst()).addPlayer(callback, clients.get(callback).nickName);
							clients.get(callback).roomName = msg.getCommandParams().getFirst();
							callback.sendMessage(new TBGPMessage("SYSMSG JOIN ACCEPTED"));
						}
						
					}
					else{
						Room room = new Room();
						room.addPlayer(callback, clients.get(callback).nickName);
						rooms.put(msg.getCommandParams().getFirst(), room);
						clients.get(callback).roomName = msg.getCommandParams().getFirst();
						callback.sendMessage(new TBGPMessage("SYSMSG JOIN ACCEPTED"));
					}	
				}else{
					callback.sendMessage(new TBGPMessage("SYSMSG JOIN REJECTED"));
				}
				break;
			}
			case "MSG":
			{
				String clientRoom = clients.get(callback).roomName;
				
				if(rooms.containsKey(clientRoom)){
					Map<ProtocolCallback<TBGPMessage>, String> playersInRoom = rooms.get(clientRoom).getPlayers();
					String data = msg.getPayload();
					for(ProtocolCallback<TBGPMessage> player : playersInRoom.keySet()){
						if(player != callback){
							player.sendMessage(new TBGPMessage("USRMSG " + data));
						}
					}
					callback.sendMessage(new TBGPMessage("SYSMSG MSG ACCEPTED"));
				}else{
					callback.sendMessage(new TBGPMessage("SYSMSG MSG REJECTED"));
				}
				
				
				break;
			}
			case "LISTGAMES":
			{
				callback.sendMessage(new TBGPMessage("SYSMSG LISTGAMES BLUFFER"));
				break;
			}
			case "STARTGAME":
			{
				if(clients.containsKey(callback) && rooms.containsKey(clients.get(callback).roomName) && !rooms.get(clients.get(callback).roomName).IsGameRunning())
				{
					String roomName = clients.get(callback).roomName;
					
					if(rooms.get(clients.get(callback).roomName).createGame(msg.getCommandParams().getFirst())){
						if(rooms.get(clients.get(callback).roomName).StartGame()){
							callback.sendMessage(new TBGPMessage("SYSMSG STARTGAME ACCEPTED"));
						}else{
							callback.sendMessage(new TBGPMessage("SYSMSG STARTGAME REJECTED"));
						}
					}else {
						callback.sendMessage(new TBGPMessage("SYSMSG STARTGAME REJECTED"));
					}
				}
				else
				{
					callback.sendMessage(new TBGPMessage("SYSMSG STARTGAME REJECTED"));
				}
				break;
			}
			case "TXTRESP":
			{
				if(clients.containsKey(callback) && rooms.containsKey(clients.get(callback).roomName) && rooms.get(clients.get(callback).roomName).IsGameRunning()){
					callback.sendMessage(new TBGPMessage("SYSMSG TXTRESP ACCEPTED"));
					rooms.get(clients.get(callback).roomName).getGame().getUserResponse(msg.getPayload(), callback);
				}else{
					callback.sendMessage(new TBGPMessage("SYSMSG TXTRESP REJECTED"));
				}
				break;
			}
			case "SELECTRESP":
			{
				if(clients.containsKey(callback) && rooms.containsKey(clients.get(callback).roomName) && rooms.get(clients.get(callback).roomName).IsGameRunning()){
					callback.sendMessage(new TBGPMessage("SYSMSG SELECTRESP ACCEPTED"));
					rooms.get(clients.get(callback).roomName).getGame().getUserSelection(msg.getPayload(), callback);
				}else{
					callback.sendMessage(new TBGPMessage("SYSMSG SELECTRESP REJECTED"));
				}
				
				break;
			}
			case "QUIT":
			{
				if(!clients.containsKey(callback)){
					callback.sendMessage(new TBGPMessage("SYSMSG QUIT ACCEPTED"));
				}else if(!rooms.containsKey(clients.get(callback).roomName)){
					clients.remove(callback);
					callback.sendMessage(new TBGPMessage("SYSMSG QUIT ACCEPTED"));
				}else if(clients.containsKey(callback) && rooms.containsKey(clients.get(callback).roomName)){
					if(rooms.get(clients.get(callback).roomName).IsGameRunning()){
						callback.sendMessage(new TBGPMessage("SYSMSG QUIT REJECTED"));
					}else{
						callback.sendMessage(new TBGPMessage("SYSMSG QUIT ACCEPTED"));
						// remove from room
						rooms.get(clients.get(callback).roomName).StopGame();
						rooms.get(clients.get(callback).roomName).removePlayer(callback);

						rooms.remove(callback);
						clients.remove(callback);		
					}		
				}
				break;
			}
			default:
			{
				callback.sendMessage(new TBGPMessage("SYSMSG " + msg.getCommand() + " UNIDENTIFIED"));
				break;
			}
			}

		}catch(IOException e){
			clients.remove(msg.getCommandParams().getFirst());
		}
				
		
	}

	@Override
	public boolean isEnd(TBGPMessage msg) {
		if(msg.getCommand().equals("QUIT") && msg.getCommandParams().getFirst().equals("ACCEPTED")){
			bShouldClose = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldClose() {
		//return bShouldClose;
		return false;
	}

	@Override
	public void connectionTerminated() {
		// Doing nothing because of the instructions - we do not need to handle connection errors.
	}
	
}
