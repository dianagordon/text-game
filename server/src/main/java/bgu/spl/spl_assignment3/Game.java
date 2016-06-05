package bgu.spl.spl_assignment3;
//import tokenizer.TBGPMessage;

//import tokenizer.TBGPMessage;

public interface Game<T> {
	public boolean startGame(Room room);
	public boolean isRunning();
	public void quit();
	public void getUserResponse(String response, ProtocolCallback<T> callback);
	public void getUserSelection(String response, ProtocolCallback<T> callback);
	
}
