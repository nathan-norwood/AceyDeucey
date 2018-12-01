package game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import java.util.Random;
import java.util.Vector;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Game {

	private int unique_id;
	private String name;

	private Vector<Card> card_deck;
	private Vector<Card> case_file;
	private Vector<Player> players;
	private HashMap<Integer, String> available_suspects;
	private double pot;
	

	/*
	 * TODO: Order of play is: scarlet, mustard, white, green, peacock, plum How
	 * should this be represented?
	 * 
	 */
	private static final Logger logger = LogManager.getLogger(Game.class);

	/* State variables */
	private boolean openGame = false;
	private Player current_player;

	
	private int curLow;
	private int curHigh;
	
	private Card card1;
	private Card card2;

	public Game(int id, String n, int h_id, String playerName) {
		logger.info("Game Constructor");
		unique_id = id;
		name = n;
		
		pot = 0;
		initCardDeck(); 
		
		players = new Vector<Player>();

		
		addPlayer(h_id, playerName);

		openGame = true;

		// TODO Set Current Player
	}

	public int getId() {
		return unique_id;
	}

	public String getName() {
		return name;
	}

	public Vector<Player> getPlayers() {
		return players;
	}


	public Player getCurrent_player() {
		return current_player;
	}

	private void initCardDeck() {
		card_deck = new Vector<Card>();
		int id = 0;

		for(Suit suit: Suit.values()){
			for(CardValue value: CardValue.values()){
				card_deck.add(new Card(id, suit, value));
				id++;
			}
		}
	
	}

	

	
	public Card getNextCard() {
		//return card_deck.get(12); //getting the ACE of Spades
		Random randomDeck = new Random();
		int next = randomDeck.nextInt(card_deck.size());
		Card card = card_deck.remove(next);
		return card;
		
	}



	public void addPlayer(int playerID, String playerName) {
		players.add(new Player(playerID, playerName,this));
		
	}


	public Vector<Response> startGame() {
	

		if (players.size() >= 2) {
			openGame = false;
			Vector<Response> responses = new Vector<Response>();
			System.out.println("Game Started!");

			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			if(current_player == null ){
				for(Player p: players){
					if(p.isActive()){
						current_player = p;
						break;
					};
				}
			}else{
				current_player = nextPlayer();
			}
			
			
			for(Player p: players){
				if(p.isActive()){
					p.addToPot(1);
				};
			}
			
			
			obuilder.add("type", "UPDATE_POT");
			obuilder.add("pot", pot);
			
			responses.add(new Response(0,obuilder.build()));
			responses.addAll(getPlayersDebt());
			
			responses.addAll(dealCurrentPlayer());
			

			/* start the game... */
			return responses;

		} else {
			/* not enough people to start the game */
			return null;
		}
	}

	
	private Vector<Response> dealCurrentPlayer() {
		JsonObjectBuilder obuilder = Json.createObjectBuilder();
		Vector<Response> responses = new Vector<Response>();
		
		
		
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", " 's turn");
		
		responses.add(new Response(0,obuilder.build()));
		if(card_deck.size()<=3){
			
			initCardDeck();
		}
			
		card1 = getNextCard();
		card2 = getNextCard();
		
		if(card1.getCardValue() == CardValue.ACE){
			obuilder.add("type", "CHECK_ACE");
			responses.add(new Response(current_player.getUniqueId(), obuilder.build()));
			
			obuilder.add("type", "MSG");
			obuilder.add("subject", current_player.getPlayerName());
			obuilder.add("msg", " 's 1st card is an ACE..checking if they want high or low");
			
			responses.add(new Response(0, obuilder.build()));
			
			
			
			obuilder.add("type", "CARDS");
			obuilder.add("card1", card1.getImagePath());
			obuilder.add("card2","images/cards/red_back.png" );
			
			responses.add(new Response(0,obuilder.build()));
			return responses;
			
		}
		
		
		curHigh = Integer.max(card1.getCardValue().getCardValue(), card2.getCardValue().getCardValue());
		curLow = Integer.min(card1.getCardValue().getCardValue(), card2.getCardValue().getCardValue());
		
		
		
		responses.addAll(finishDeal(new Vector<Response>()));
		
		
		
		return responses;
	}
	

	public Vector<Response> setAceAndFinishDeal(String isHigh) {
		String highOrLow;
		if(isHigh.equals("true")){
			curHigh = CardValue.ACE.getCardValue();
			curLow = card2.getCardValue().getCardValue();
			highOrLow = "HIGH";
		}else{
			curLow = 1;
			curHigh = card2.getCardValue().getCardValue();
			highOrLow = "LOW";
		}
		JsonObjectBuilder obuilder = Json.createObjectBuilder();
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", " want their Ace to be " + highOrLow);
		
		Vector<Response> responses = new Vector<Response>();
		responses.add(new Response(0,obuilder.build()));
		
		return finishDeal(responses);
		
	}
	
	private Vector<Response> finishDeal(Vector<Response> responses){
		JsonObjectBuilder obuilder = Json.createObjectBuilder();
		
		obuilder.add("type", "CARDS");
		obuilder.add("card1", card1.getImagePath());
		obuilder.add("card2", card2.getImagePath());
		
		responses.add(new Response(0,obuilder.build()));
		
		responses.add(new Response(current_player.getUniqueId(), obuilder.add("type", "TURN").build()));
		
		System.out.println("currentHigh: " + curHigh);
		System.out.println("currentLow: " + curLow);
		
		return responses;
	}

	

	
	public Vector<Response> currentPlayerPass(){
		Vector<Response> responses = new Vector<Response>();
		JsonObjectBuilder obuilder = Json.createObjectBuilder();
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", " Passes");
		
		responses.add(new Response(0,obuilder.build()));
		
		current_player = nextPlayer();
		
		responses.addAll(dealCurrentPlayer());
		return responses;
	}


	public Player nextPlayer() {
		/* TODO: Move current player to next player & notify them */
		int index = players.indexOf(current_player);
		Player p;
		do {
			p = players.get((index + 1) % players.size());
			index++;
		} while (!p.isActive());

		return p;
	}

	public boolean isOpen() {
		return openGame;
	}

	private Vector<Response> getPlayersDebt(){
		Vector<Response> responses = new Vector<Response>();
		for(Player p: players){
			
			responses.add(p.getDebt());
		}
		
		return responses;
	}

	public void addToPot(double amount) {
		pot+=amount;
		
	}
	
	public void subtractFromPot(double amount){
		pot -= amount;
	}

	/**
	 * Player is playing
	 * Check if they won and adjust pot accordingly
	 * @param int1
	 * @return
	 */
	public Vector<Response> currentPlayerPlay(int bet) {
		
		Card thirdCard = this.getNextCard();
		
		
		Vector<Response> responses = new Vector<Response>();
		JsonObjectBuilder obuilder = Json.createObjectBuilder();
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", " is Playing and Bets " + bet);
		
		
		
		responses.add(new Response(0,obuilder.build()));

		

		int thirdCardValue = thirdCard.getCardValue().getCardValue();
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", " 's flip card is " + thirdCard.getName() + " with " +card1.getName() + " and " + card2.getName() + " showing");
		responses.add(new Response(0,obuilder.build()));
		
		obuilder.add("type", "CARDS");
		obuilder.add("card1", card1.getImagePath());	
		obuilder.add("card2", card2.getImagePath());
		obuilder.add("playerCard", thirdCard.getImagePath());
		responses.add(new Response(0,obuilder.build()));

		String playerResult = "";
		if(thirdCardValue > curLow && thirdCardValue<curHigh){
			//Winner
			if(bet == this.pot){
				//Round over
				obuilder.add("type", "MSG");
				obuilder.add("subject", current_player.getPlayerName());
				obuilder.add("msg", " 's wins the pot, round over");
				responses.add(new Response(0,obuilder.build()));
				this.getCurrent_player().takeFromPot(bet);
				responses.add(current_player.getDebt());
				obuilder.add("type", "UPDATE_POT");
				obuilder.add("pot", pot);
				responses.add(new Response(0,obuilder.build()));
				
				return responses;
			}else{
				//subtract from pot and move to next player
				this.current_player.takeFromPot(bet);
				playerResult =  " wins " + bet + "pot is now " + pot;
			}
		}else if(thirdCardValue == curLow || thirdCardValue == curHigh){
			this.current_player.addToPot(bet *2);
			playerResult =  " POSTS! " + "Pot is now " + pot;
		}else{
			//Lose bet to pot
			this.current_player.addToPot(bet);
			playerResult = "loses. Pot its now + " + pot;
		}
		
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", playerResult);
		responses.add(new Response(0,obuilder.build()));
		

		
				
		obuilder.add("type", "UPDATE_POT");
		obuilder.add("pot", pot);
		responses.add(new Response(0,obuilder.build()));
		
		responses.add(current_player.getDebt());
		current_player = nextPlayer();
		

		responses.addAll(dealCurrentPlayer());
		
		return responses;
	}

}
