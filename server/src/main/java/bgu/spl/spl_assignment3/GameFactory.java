package bgu.spl.spl_assignment3;
public class GameFactory {
	public GameFactory(){};
	public Game createGame(String name) {
		if(name.equals("BLUFFER")){
			return new BlufferGame();
		}else{
			return null;
		}
	}
}
