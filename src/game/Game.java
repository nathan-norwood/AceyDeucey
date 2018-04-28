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
	private GameBoard board;

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

	private Player disproving_player = null;
	private JsonObject suggestion;
	
	private int curLow;
	private int curHigh;
	
	private Card card1;
	private Card card2;

	public Game(int id, String n, int h_id, String playerName) {
		logger.info("Game Constructor");
		unique_id = id;
		name = n;
		board = new GameBoard();
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

	public HashMap<Integer, String> getAvailableSuspects() {
		return available_suspects;
	}

	public GameBoard getGameBoard() {
		return board;
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

		Random randomDeck = new Random();
		/* TODO: validate nextInt doesnt over reach index */
		if (card_deck.size() > 0) {
			int next = randomDeck.nextInt(card_deck.size());
			Card card = card_deck.remove(next);
			return card;
		} else {
			return null;
		}
	}



	public void addPlayer(int playerID, String playerName) {
		players.add(new Player(playerID, playerName,this));
		
	}


	public Vector<Response> startGame() {
		// Should never happen, validation on front end

		if (players.size() >= 2) {
			openGame = false;
			Vector<Response> responses = new Vector<Response>();
			System.out.println("Game Started!");

			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			
			current_player = players.get(0);			
			players.forEach(p-> p.addToPot(1));
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
		responses.add(new Response(current_player.getUniqueId(), obuilder.add("type", "TURN").build()));
		
		
		obuilder.add("type", "MSG");
		obuilder.add("subject", current_player.getPlayerName());
		obuilder.add("msg", " 's turn");
		
		responses.add(new Response(0,obuilder.build()));
		
		card1 = getNextCard();
		
		
		if(card1.getCardValue() == CardValue.ACE){
			//Check with player if they want high or low
		}
		card2 = getNextCard();
		
		obuilder.add("type", "CARDS");
		obuilder.add("card1", card1.getName());
		obuilder.add("card2", card2.getName());
		
		
		responses.add(new Response(0,obuilder.build()));
		
		
		
		return responses;
	}

	private String getCardSrcById(int id) {
		String imgSrc = "images/cards/";
		for (GameComponent gc : board.getGameComponenets()) {
			if (gc.getId() == id) {
				imgSrc += gc.getImgName();
			}
		}

		return imgSrc;
	}

	/* ask Player to disprove a suggestion */

	
	
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

		JsonObjectBuilder builder = Json.createObjectBuilder();
		for(Player p: players){
			builder.add("type", "UPDATE_DEBT");
			builder.add("debt", p.getAmountOwed());
			responses.add(new Response(p.getUniqueId(),builder.build()));
		}
		
		return responses;
	}

	public void addToPot(double amount) {
		pot+=amount;
		
	}
}
