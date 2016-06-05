package bgu.spl.spl_assignment3.tokenizer;

import java.util.LinkedList;
import java.util.StringJoiner;

public class TBGPMessage implements Message<TBGPMessage> {
	//private final String message;
	private String command;
	private LinkedList<String> params;
	
	public TBGPMessage(String message){
		params = new LinkedList<String>();
		if(message.indexOf(' ') > -1){
			command = message.substring(0, message.indexOf(' '));
			if(message.indexOf(' ') > -1){
				message = message.substring(message.indexOf(' ') + 1);
			}
		}else{
			command = message;
		}
		
		
		while(message.indexOf(' ') > -1){
			params.add(message.substring(0, message.indexOf(' ')));
			message = message.substring(message.indexOf(' ') + 1);
		}
		
		params.add(message);
	}

	public String getCommand(){
		return command;
	}
	
	public String getPayload(){
		StringJoiner joiner = new StringJoiner(" ");
		for(String s : params){
			joiner.add(s);
		}
		return joiner.toString();
	}
	public LinkedList<String> getCommandParams(){
		return params;
	}
	public String getMessage(){
		//String message(command + params.to)
		StringJoiner joiner = new StringJoiner(" ");
		joiner.add(command);
		for(String s : params){
			joiner.add(s);
		}
		return joiner.toString();//.concat("\n");
	}
	
	@Override
	public String toString(){
		return getMessage();
	}
}
