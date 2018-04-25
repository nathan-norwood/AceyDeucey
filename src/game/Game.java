package game;

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

	public Game(int id, String n, int h_id, int s_id) {
		logger.info("Game Constructor");
		unique_id = id;
		name = n;
		board = new GameBoard();

		initCardDeck(); // Must happen after board is initialized
		initCaseFile(); // Must happen after card_deck is initialized
		System.out.println(case_file);
		players = new Vector<Player>();

		available_suspects = board.getSuspects();
		addPlayer(h_id, s_id);

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

		/*
		 * TODO: Note that instead of getting the id/name, we could just use the
		 * object... Decide later.
		 */
		for (Entry<Integer, String> e : board.getSuspects().entrySet()) {
			card_deck.add(new Card(GameComponentType.SUSPECT, e.getKey().intValue(), e.getValue()));
		}

		for (Entry<Integer, String> e : board.getWeapons().entrySet()) {
			card_deck.add(new Card(GameComponentType.WEAPON, e.getKey().intValue(), e.getValue()));

		}
		for (Entry<Integer, String> e : board.getRooms().entrySet()) {
			card_deck.add(new Card(GameComponentType.ROOM, e.getKey().intValue(), e.getValue()));

		}

	}

	private void initCaseFile() {
		case_file = new Vector<Card>();
		case_file.add(getNextCardOfType(GameComponentType.SUSPECT));
		case_file.add(getNextCardOfType(GameComponentType.WEAPON));
		case_file.add(getNextCardOfType(GameComponentType.ROOM));

	}

	/*
	 * TODO: Get cards and assign them to players... once assigned, deck is
	 * empty
	 */
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

	/* TODO: Use this function to select the case file */
	public Card getNextCardOfType(GameComponentType t) {

		Random randomDeck = new Random();

		int next = randomDeck.nextInt(card_deck.size());
		while (card_deck.get(next).getType() != t) {
			next = randomDeck.nextInt(card_deck.size());
		}
		Card card = card_deck.remove(next);
		return card;
	}

	public boolean addPlayer(int playerID, int suspectID) {
		/*
		 * TODO: what do we need to send in to add a player to this game? 1.
		 * Player ID - where GameServelet keeps ID + socket map, and game keeps
		 * ID + player object?
		 */
		if (players.size() < 6) {
			players.add(new Player(playerID, suspectID));
			available_suspects.remove(suspectID);
			if (players.size() == 6) {
				/* close game once max reached */
				openGame = false;
			}
			return true;
		} else {
			return false;
		}
		/*
		 * TODO: currently players are added in order of join making the host
		 * the first player always - if we want them in a different order then
		 * we need to do an additional step here.
		 */

	}

	public void dealCards() {
		/* TODO: Base logic is here, Validate this works! */
		boolean cont = true;
		do {
			for (Player p : players) {
				Card c = getNextCard();
				if (c != null) {
					p.addCard(c);
				} else {
					cont = false;
					break;
				}
			}
		} while (cont);

	}

	public Vector<Response> startGame() {
		// Should never happen, validation on front end

		if (players.size() >= 2) {
			openGame = false;
			Vector<Response> responses = new Vector<Response>();
			System.out.println("Game Started!");

			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			dealCards();
			for (Player p : players) {
				if (p.getSuspectId() == board.getScarlet()) {
					current_player = p;
				}
				obuilder.add("type", "CARDS");
				for (Card c : p.getCards()) {
					abuilder.add(Json.createObjectBuilder().add("id", c.getId()).add("name", c.getName()).add("img",
							getCardSrcById(c.getId())));
				}
				obuilder.add("cards", abuilder);
				Response r = new Response(p.getUniqueId(), obuilder.build());
				responses.add(r);

			}
			if (current_player == null) {
				current_player = players.get(0);
			}

			obuilder.add("type", "MSG");
			obuilder.add("suspect", current_player.getSuspectId());
			obuilder.add("msg", " will have the first move.");

			Response r = new Response(0, obuilder.build());
			responses.add(r);
			responses.add(new Response(0, getBoardState()));
			responses.add(new Response(current_player.getUniqueId(), sendMove()));

			/* start the game... */
			return responses;

		} else {
			/* not enough people to start the game */
			return null;
		}
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

	/* process disprove response */
	public Vector<Response> processDisproveResponse(int id) {
		Vector<Response> responses = new Vector<Response>();
		if (disproving_player != null) {
			if (id != -1 && (suggestion.getInt("suspect") == id || suggestion.getInt("weapon") == id
					|| suggestion.getInt("room") == id)) {
				// disprove successful
				// Notify current_player of card that was successful & ask if
				// they want to do an accusation
				Card c = disproving_player.getCardById(id);
				if (c != null) {
					responses.addElement(new Response(current_player.getUniqueId(),
							Json.createObjectBuilder().add("type", "TURN2")
									.add("suspect", disproving_player.getSuspectId())
									.add("notice", "Card for " + c.getName() + " shown by ").build()));
					// Notify all players that it proving suspect ID disproved
					// current_player ** need a new object
					responses.add(new Response(0,
							Json.createObjectBuilder().add("type", "MSG")
									.add("suspect", disproving_player.getSuspectId())
									.add("msg", "successfully disproved suggestion.").build()));
					disproving_player = null;
				} else {
					// TODO Error!
				}
			} else {
				// Notify everyone that they cant disprove.
				responses.add(new Response(0, Json.createObjectBuilder().add("type", "MSG")
						.add("suspect", disproving_player.getSuspectId()).add("msg", "cannot disprove.").build()));

				int index = players.indexOf(disproving_player);
				disproving_player = players.get((index + 1) % players.size());

				if (!disproving_player.equals(current_player)) {
					responses.add(new Response(disproving_player.getUniqueId(),
							Json.createObjectBuilder().add("type", "DISPROVE").add("suggestion", suggestion).build()));
				} else {
					// notify cannot be disproven
					disproving_player = null;
					suggestion = null;
					responses.addElement(new Response(current_player.getUniqueId(), Json.createObjectBuilder()
							.add("type", "TURN2").add("notice", "No one could disprove your suggestion!").build()));
					// Notify everyone that no disprove.
					responses.add(new Response(0, Json.createObjectBuilder().add("type", "MSG")
							.add("msg", "No one could disprove suggestion!").build()));

				}
			}

		} else {
			// TODO Error!
		}
		return responses;

	}

	public Vector<Response> processAccusationReponse(JsonArray accusation) {
		// accusation: [3, 12, 6]
		Vector<Response> responses = new Vector<Response>();
		boolean successful = true;
		Vector<String> stringAcc = new Vector<String>();
		for (Object o : accusation) {
			stringAcc.add(o.toString());
		}
		/*
		 * TODO: 0. Only if disproving_player == null, can make a accusation 1.
		 * Compare accusation against case file 2. if valid, end game, notify
		 * players of win 3. if invalid, deactivate player, notify players of
		 * deactivation
		 */
		if (!accusation.isEmpty()) {
			if (disproving_player == null) {
				for (Card c : case_file) {
					if (!stringAcc.contains(Integer.toString(c.getId()))) {
						successful = false;
					}
				}
				if (successful) {
					String win = "";

					for (Card c : case_file) {
						win += c.getName() + " ";
					}
					responses.add(new Response(0,
							Json.createObjectBuilder().add("type", "ENDGAME")
									.add("suspect", current_player.getSuspectId())
									.add("msg", "has won the game! Solution:" + win).build()));
					// end game
				} else {
					current_player.setInactive();
					Vector<Player> activePlayers = new Vector<Player>();
					for (Player p : players) {
						if (p.isActive())
							activePlayers.add(p);
					}

					if (activePlayers.size() == 1) {
						responses.add(new Response(0,
								Json.createObjectBuilder().add("type", "ENDGAME")
										.add("suspect", activePlayers.get(0).getSuspectId())
										.add("msg",
												"has won the game by default because every other player is inactive")
										.build()));
					} else {

						responses.add(new Response(0, Json.createObjectBuilder().add("type", "MSG")
								.add("suspect", current_player.getSuspectId())
								.add("msg", "has made an incorrect accusation - they are now inactive").build()));
						current_player = nextPlayer();
						responses.add(new Response(current_player.getUniqueId(), sendMove()));
					}

				}

			}
		} else {
			current_player = nextPlayer();
			responses.add(new Response(current_player.getUniqueId(), sendMove()));
		}
		// All turns end with accusation - get next player
		return responses;

	}

	/*
	 * TODO: where are available moves identified... it changes based on game
	 * state...
	 */
	public Vector<Response> processMoveResponse(JsonObject turn) {
		Vector<Response> responses = new Vector<Response>();
		// { location: 3, suggestion:{suspect:2, weapon 4, location: 6}}

		if (disproving_player == null) {
			/*
			 * TODO: 0. Only if disproving_player == null, can make a move 1.
			 * Update board with move 2. Notify all players 3. If suggestion
			 * made, call make suggestion.
			 */
			Suspect cur_suspect = board.getSuspectById(current_player.getSuspectId());
			cur_suspect.setMovedBySuggestion(false);

			int l_id = cur_suspect.getLocation().getId();
			if (turn.containsKey("location")) {
				l_id = turn.getInt("location");
				boolean valid = false;
				for (Location l : cur_suspect.getLocation().getAvailableLocationMoves()) {
					if (l_id == l.getId()) {
						valid = true;
					}
				}
				if (valid) {
					if (!cur_suspect.getLocation().isRoom()) {
						cur_suspect.getLocation().setOccupied(false);
					}
					cur_suspect.setLocation(board.getLocationById(l_id));
					if (!cur_suspect.getLocation().isRoom()) {
						cur_suspect.getLocation().setOccupied(true);
					}
					// TODO: Notify Players of board
					// NN - Commented out b/c it was causing null pointer
					// and we aren't handling BOARD_STATE messages in the front
					responses.add(new Response(0, getBoardState()));

					JsonObjectBuilder obuilder = Json.createObjectBuilder();
					String the = (cur_suspect.getLocation().isRoom()) ? "the " : "";
					obuilder.add("type", "MSG").add("suspect", getCurrent_player().getSuspectId()).add("msg",
							"moved to " + the + getGameBoard()
									.getLocationById(l_id).getName());
					Response r = new Response(0, obuilder.build());
					responses.add(r);

				} else {
					// TODO Error!
					System.out.println("Should not be here, player made invalid move");
				}
			}
			if (turn.containsKey("suggestion")) {
				JsonObject suggest = turn.getJsonObject("suggestion");

				if (suggest.containsKey("room") && suggest.containsKey("suspect") && suggest.containsKey("weapon")
						&& l_id == suggest.getInt("room")) {

					Suspect s = board.getSuspectById(suggest.getInt("suspect"));
					s.getLocation().setOccupied(false);
					s.setLocation(board.getLocationById(l_id));
					if (!s.getLocation().isRoom()) {
						s.getLocation().setOccupied(true);
					}
					s.setMovedBySuggestion(true);
					Weapon w = board.getWeaponById(suggest.getInt("weapon"));
					w.setLocation(board.getLocationById(l_id));

					// TODO Notify Players of board updates & suggestion
					responses.add(new Response(0, getBoardState()));
					responses.add(new Response(0,
							Json.createObjectBuilder().add("type", "MSG").add("suspect", current_player.getSuspectId())
									.add("msg",
											"has made a suggestion: "
													+ board.getSuspectById(suggest.getInt("suspect")).getName() + ", "
													+ board.getWeaponById(suggest.getInt("weapon")).getName() + ", "
													+ board.getLocationById(suggest.getInt("room")).getName())
									.build()));

					// TODO Start Disprove Process
					disproving_player = current_player;
					int index = players.indexOf(disproving_player);
					disproving_player = players.get((index + 1) % players.size());

					this.suggestion = suggest;
					responses.add(new Response(disproving_player.getUniqueId(),
							Json.createObjectBuilder().add("type", "DISPROVE").add("suggestion", suggestion).build()));

				} else {
					// TODO Error!
					System.out.println("Malformed JSON object selection");
				}
			} else {

				/* If no suggestion made, send back option to accuse */
				responses.add(new Response(current_player.getUniqueId(),
						Json.createObjectBuilder().add("type", "TURN2").build()));
			}

		} else {
			// TODO Error!
			System.out.println("Disproving Player is not null");
		}

		return responses;

	}

	private JsonObject getBoardState() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", "BOARD_STATE");

		JsonArrayBuilder abuilder = Json.createArrayBuilder();
		for (Weapon w : board.getWeaponSet()) {
			abuilder.add(Json.createObjectBuilder().add("r_id", w.getLocation().getId()).add("type", "weapon").add("id",
					w.getId()));
		}
		for (Suspect s : board.getSuspectSet()) {
			abuilder.add(Json.createObjectBuilder().add("r_id", s.getLocation().getId()).add("type", "suspect")
					.add("id", s.getId()));
		}
		builder.add("board", abuilder);

		return builder.build();
	}

	/*
	 * First Move check to see if suspect is in room check to see if player is
	 * there by suggestion check to see if hallways occupied options: move to
	 * empty hallway/room make suggestion (if there by suggestion) make
	 * accusation check to see if suspect is in hallway options: move to
	 * available rooms, make suggestion make accusation
	 * 
	 * Second Move Make accusation
	 * 
	 */

	public JsonObject sendMove() {
		Suspect s = board.getSuspectById(current_player.getSuspectId());

		// JSON: { type: TURN, options:{ location:[{id:1, name:"H1",
		// room:false}, ...], suggestion:true} //accusation is always true

		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (Location l : s.getLocation().getAvailableLocationMoves()) {
			array.add(Json.createObjectBuilder().add("id", l.getId()).add("name", l.getName()).add("room", l.isRoom()));
		}
		// NN changed to "locations" since I have a weird pet peeve about
		// iterating over plurals haha
		builder.add("locations", array);
		builder.add("suggestion", (s.getLocation().isRoom() && s.wasMovedBySuggestion()));

		JsonObject json = Json.createObjectBuilder().add("type", "TURN").add("options", builder).build();
		return json;

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

}
