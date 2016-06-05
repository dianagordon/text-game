package bgu.spl.spl_assignment3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import bgu.spl.spl_assignment3.tokenizer.*;

//import tokenizer.TBGPMessage;
public class BlufferGame implements Game<TBGPMessage> {
	//private Map<String, Integer> scores;
	private class playerDetails{
		public int score;
		public String name;
	}
	
	private class QuestionDetail{
		public String question;
		public String realAnswer;
		public Map<String, LinkedList<String>> fakeAnswers;
		
		public QuestionDetail(){
			fakeAnswers = new ConcurrentHashMap<String, LinkedList<String>>();
		}
	}
	LinkedList<playerDetails> scores;
	//Map<String, <String, LinkedList<String>>> questions;
	ArrayList<QuestionDetail> questions;
	private boolean isRunning;
	private int currentQuestion;
	private int numAnswersReceived;
	private Room room;
	private List<String> shuffledAnswers;
	private static String jsonFilePath = "questions.json";
	
	public BlufferGame(){
		isRunning = false;
		currentQuestion = 0;
		numAnswersReceived = 0;
		questions = new ArrayList<QuestionDetail>();
		scores = new LinkedList<playerDetails>();
	}

	@Override
	public boolean isRunning(){
		return isRunning;
	}

	@Override
	public boolean startGame(Room room) {
		isRunning = true;
		this.room = room;
		
		for(String player : room.getPlayers().values()){
			playerDetails playerDetails = new playerDetails();
			playerDetails.name = player;
			playerDetails.score = 0;
			scores.add(playerDetails);
		}
		
		numAnswersReceived = 0;
		
		// Read json		
		Gson gson = new Gson();
		String jsonFileName = jsonFilePath;
		
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(jsonFileName));
			
			String jsonString = new String(encoded, StandardCharsets.UTF_8);
			JsonFileStructure parsedJson = gson.fromJson(jsonString, JsonFileStructure.class);
			
			
			// insert into data types
			Random randomGenerator = new Random();
			
		    for (int i = 0; i < 3 && parsedJson.questions.size() > 0; i++){
		        int randomInt = randomGenerator.nextInt(parsedJson.questions.size());
		        QuestionDetail newQuestionLoaded = new QuestionDetail();
		        newQuestionLoaded.question = parsedJson.questions.get(randomInt).questionText;
		        newQuestionLoaded.realAnswer = parsedJson.questions.get(randomInt).realAnswer;
		        
		        questions.add(newQuestionLoaded);
		        parsedJson.questions.remove(randomInt);
		    }
		    
			if(questions.size() < 3){
				isRunning = false;
				return false;
			}
			
			sendNewQuestion();
			
			return true;
			
		} catch (IOException e1) {
			System.out.println("Error");
			e1.printStackTrace();
			isRunning = false;
			return false;
		}
		
	}

	private void sendNewQuestion(){
		for(ProtocolCallback<TBGPMessage> player : room.getPlayers().keySet()){
			try {
				player.sendMessage(new TBGPMessage("ASKTXT "+ questions.get(currentQuestion).question));
			} catch (IOException e) {
				System.out.println("Error");
				e.printStackTrace();
			}
		}
	}
	@Override
	public void quit() {
		isRunning = false;
		
	}

	@Override
	public void getUserResponse(String response, ProtocolCallback<TBGPMessage> callback) {
		if(!questions.get(currentQuestion).fakeAnswers.containsKey(response)){
			questions.get(currentQuestion).fakeAnswers.put(response, new LinkedList<String>());
		}
		
		questions.get(currentQuestion).fakeAnswers.get(response).add(room.getPlayers().get(callback));
		numAnswersReceived++;
		
		if(numAnswersReceived == room.getPlayers().size()){
			shuffledAnswers = new ArrayList<String>(questions.get(currentQuestion).fakeAnswers.keySet());
			shuffledAnswers.add(questions.get(currentQuestion).realAnswer);
			Collections.shuffle(shuffledAnswers);
			
			
			String askChoices = new String("ASKCHIOCES ");
			Integer i = new Integer(0);
			for(String answer : shuffledAnswers){
				askChoices += i.toString() + ". ";
				askChoices += answer.toLowerCase() + " ";
				i++;
			}
			
			for(ProtocolCallback<TBGPMessage> player : room.getPlayers().keySet()){
				try {
					player.sendMessage(new TBGPMessage(askChoices));
				} catch (IOException e) {
					System.out.println("Error");
					e.printStackTrace();
				}
			}
			
			numAnswersReceived = 0;
		}	
	}

	@Override
	public void getUserSelection(String response, ProtocolCallback<TBGPMessage> callback) {
		
		try {
			callback.sendMessage(new TBGPMessage("GAMEMSG The correct answer is: "+ questions.get(currentQuestion).realAnswer));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		//ArrayList<String> userAnswers = new ArrayList<String>(questions.get(currentQuestion).fakeAnswers.keySet());
		//userAnswers.add(questions.get(currentQuestion).realAnswer);
		if(Integer.parseInt(response) < shuffledAnswers.size() && shuffledAnswers.get(Integer.parseInt(response)).equals(questions.get(currentQuestion).realAnswer)){
			try {
				callback.sendMessage(new TBGPMessage("GAMEMSG Correct! +10pts"));
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ERROR");
			}
			for(playerDetails playerScore : scores){
				if(playerScore.name.equals(room.getPlayers().get(callback))){
					playerScore.score += 10;
				}
			}
		}else{
			try {
				callback.sendMessage(new TBGPMessage("GAMEMSG Incorrect answer"));
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ERROR");
			}
			Integer i = new Integer(0);
			for(String fakeAnswer : shuffledAnswers){
				if(response.equals(i.toString())){
					LinkedList<String> winners = questions.get(currentQuestion).fakeAnswers.get(fakeAnswer);
					for(String winner : winners){
						for(playerDetails playerScore : scores){
							if(playerScore.name.equals(winner) && !winner.equals(room.getPlayers().get(callback))){
								playerScore.score += 5;
							}
						}
					}
				}
				i++;
			}
		}
		
		numAnswersReceived++;
		
		if(numAnswersReceived == room.getPlayers().size() && currentQuestion < 3){
			currentQuestion++;
			numAnswersReceived = 0;
			if(currentQuestion < 3){
				sendNewQuestion();
			}
		}
		
		if(currentQuestion == 3 && numAnswersReceived == 0){
			String summary = new String("GAMEMSG Summary: ");
			for(playerDetails playerScore : scores){
				summary += playerScore.name + ": " + playerScore.score + "pts ";
			}
			
			for(ProtocolCallback<TBGPMessage> player : room.getPlayers().keySet()){
				try {
					player.sendMessage(new TBGPMessage(summary));
				} catch (IOException e) {
					System.out.println("Error");
					e.printStackTrace();
				}
			}
			
			isRunning = false;
			System.out.println("game finished");
		}
	}

}