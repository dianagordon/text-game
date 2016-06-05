package bgu.spl.spl_assignment3;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.spl_assignment3.tokenizer.*;

public class Room{
		private Map<ProtocolCallback<TBGPMessage>, String> players;
		GameFactory gameFactory;
		private Game game;
		public Room(){
			gameFactory = new GameFactory();
			players = new ConcurrentHashMap<ProtocolCallback<TBGPMessage>, String>();
		}
		public boolean IsGameRunning(){
			if(game == null){
				return false;
			}else{
				return game.isRunning();
			}
		}
		
		public Game getGame(){
			return game;
		}
		
		public boolean StartGame(){
			if(game == null){
				return false;
			}
			return game.startGame(this);
		}
		
		public boolean StopGame(){
			if(game == null){
				return false;
			}
			
			game.quit();
			return true;
		}
		public boolean createGame(String gameName){
			game = gameFactory.createGame(gameName);
			
			if(game == null){
				return false;
			}
			
			
			return true;
		}
		public Map<ProtocolCallback<TBGPMessage>, String> getPlayers(){
			return players;
		}
		public boolean addPlayer(ProtocolCallback<TBGPMessage> callback, String name){
			if(!players.containsKey(callback)){
				players.put(callback, name);
				return true;
			}
					
			return false;
		}
		
		public void removePlayer(ProtocolCallback<TBGPMessage> callback){
			if(IsGameRunning()){
				StopGame();	
			}
			
			players.remove(callback);
		}
	}