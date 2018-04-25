package game;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.websocket.Session;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/* Singleton that manages games */
public class GameManager {

	private Vector<Game> games;
	private GameBoard game_board;
	private static GameManager instance;
	private int id = 0;
	private BiMap<Session, Integer> playerSessions = HashBiMap.create();

	private GameManager() {
		games = new Vector<Game>();
		game_board = new GameBoard();
	}

	public static GameManager getInstance() {
		if (instance == null) {
			instance = new GameManager();
		}
		return instance;
	}

	public void newGame(String name, int hostPlayerId, int suspectId) {
		/* Create a new game */

		/*
		 * TODO: Instead of creating the game and deleting it if the host
		 * doesn't join, this function only creates a game with the host player
		 * joined to it. Update later if time allows
		 */
		games.add(new Game(id, name, hostPlayerId, suspectId));
		id++;

	}

	public Game getGameById(int id) {
		/* When a specific game is selected, use it */

		for (Game g : games) {
			if (g.getId() == id) {
				return g;
			}
		}
		return null;
	}

	public void addSession(Session session, int id) {
		this.playerSessions.put(session, id);
	}

	public void handleMessage(Session session, String message) {

		System.out.println("Game Manager handling: " + message + "from player " + playerSessions.get(session));
		JsonReader reader = Json.createReader(new StringReader(message));
		JsonObject input = reader.readObject();

		// System.out.println("Game Manager JSON: "+input.getString("type"));

		reader.close();

		if (!input.containsKey("type")) {
			// TODO Error!
		}

		// Check to see what the "type" is and perform operations accordingly
		if (input.getString("type").equals("GET_SETUP")) {
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			obuilder.add("type", "SETUP");

			for (Game g : games) {
				if (g.isOpen()) {
					abuilder.add(Json.createObjectBuilder().add("id", g.getId()).add("name", g.getName()));
				}
			}
			obuilder.add("games", abuilder);
			abuilder = Json.createArrayBuilder();

			for (Suspect s : game_board.getSuspectSet()) {
				abuilder.add(Json.createObjectBuilder().add("id", s.getId()).add("name", s.getName()).add("img",
						s.getImgName()));

			}
			obuilder.add("suspects", abuilder);
			abuilder = Json.createArrayBuilder();

			for (Weapon w : game_board.getWeaponSet()) {
				abuilder.add(Json.createObjectBuilder().add("id", w.getId()).add("name", w.getName()).add("img",
						w.getImgName()));

			}
			obuilder.add("weapons", abuilder);
			abuilder = Json.createArrayBuilder();

			for (Location l : game_board.getRoomSet()) {
				abuilder.add(Json.createObjectBuilder().add("id", l.getId()).add("name", l.getName()).add("img",
						l.getImgName()));

			}
			obuilder.add("rooms", abuilder);

			try {
				session.getBasicRemote().sendText(obuilder.build().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (input.getString("type").equals("CREATE")) {
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			Game g = new Game(id++, input.getString("name"), playerSessions.get(session), input.getInt("suspect"));
			games.add(g);
			obuilder.add("type", "LOBBY");
			obuilder.add("gameId", g.getId());
			obuilder.add("gameName", g.getName());
			obuilder.add("isHost", true);

			for (Player p : g.getPlayers()) {
				Suspect s = g.getGameBoard().getSuspectById(p.getSuspectId());
				abuilder.add(Json.createObjectBuilder().add("id", s.getId()).add("name", s.getName()));
			}
			obuilder.add("suspects", abuilder);
			// System.out.println(obuilder.build().toString());
			try {
				session.getBasicRemote().sendText(obuilder.build().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(input.getInt("suspect"));

		} else if (input.getString("type").equals("GET_SUSPECTS")) {
			Game g = games.get(Integer.parseInt(input.getString("game")));

			// System.out.println(g.getAvailableSuspects());
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			obuilder.add("type", "AVAIL_SUSPECTS");
			for (Entry<Integer, String> e : g.getAvailableSuspects().entrySet()) {
				abuilder.add(Json.createObjectBuilder().add("id", e.getKey()).add("name", e.getValue()));

			}
			obuilder.add("suspects", abuilder);
			// System.out.println(obuilder.build().toString());
			try {
				session.getBasicRemote().sendText(obuilder.build().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (input.getString("type").equals("JOIN")) {
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			Game g = games.get(Integer.parseInt(input.getString("game")));
			g.addPlayer(playerSessions.get(session), input.getInt("suspect"));
			obuilder.add("type", "LOBBY");
			obuilder.add("gameId", g.getId());
			obuilder.add("gameName", g.getName());
			obuilder.add("isHost", false);

			for (Player p : g.getPlayers()) {
				Suspect s = g.getGameBoard().getSuspectById(p.getSuspectId());
				abuilder.add(Json.createObjectBuilder().add("id", s.getId()).add("name", s.getName()));
			}
			obuilder.add("suspects", abuilder);
			String msg = obuilder.build().toString();
			try {
				for (Player p : g.getPlayers()) {
					Session ses = playerSessions.inverse().get(p.getUniqueId());
					ses.getBasicRemote().sendText(msg);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (input.getString("type").equals("START")) {
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			Game g = games.get(input.getInt("game"));
			Vector<Response> responses = g.startGame();
			sendResponses(responses, g);

		} else if (input.getString("type").equals("TURN")) {
			// Send 'game' with each msg.
			Vector<Response> responses;
			Game g = games.get(input.getInt("game"));
			

			responses = g.processMoveResponse(input.getJsonObject("selection"));
			sendResponses(responses, g);

		} else if (input.getString("type").equals("DISPROVE")) {
			Vector<Response> responses;
			Game g = games.get(input.getInt("game_id"));
			responses = g.processDisproveResponse(Integer.parseInt(input.getString("card")));
			sendResponses(responses, g);

		} else if (input.getString("type").equals("ACCUSE")) {
			Vector<Response> responses;
			Game g = games.get(input.getInt("game_id"));
			responses = g.processAccusationReponse(input.getJsonArray("accusation"));
			sendResponses(responses, g);
		} else {
			System.out.println("Bad JSON" + input);
		}

	}

	private void sendToAllPlayers(Response res, Game g) {
		System.out.println(res.getMsgs());
		for (Player p : g.getPlayers()) {
			Session ses = playerSessions.inverse().get(p.getUniqueId());
			try {
				// obuilder.build() clears the obuilder
				ses.getBasicRemote().sendText(res.getMsgs().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void sendToSpecificPlayer(Response res) {
		Session ses = playerSessions.inverse().get(res.getSession_id());
		System.out.println(res.getMsgs());
		try {
			// obuilder.build() clears the obuilder
			ses.getBasicRemote().sendText(res.getMsgs().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendResponses(Vector<Response> responses, Game g) {
		for (Response res : responses) {
			if (res.getSession_id() == 0) {
				sendToAllPlayers(res, g);
			} else {
				sendToSpecificPlayer(res);
			}
		}

	}
}
