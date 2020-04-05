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

	private static GameManager instance;
	private int id = 0;
	private BiMap<Session, Integer> playerSessions = HashBiMap.create();

	private GameManager() {
		games = new Vector<Game>();

	}

	public static GameManager getInstance() {
		if (instance == null) {
			instance = new GameManager();
		}
		return instance;
	}

	/*public void newGame(String name, int hostPlayerId, int suspectId) {
		 Create a new game 

		
		 * TODO: Instead of creating the game and deleting it if the host
		 * doesn't join, this function only creates a game with the host player
		 * joined to it. Update later if time allows
		 
		games.add(new Game(id, name, hostPlayerId, suspectId));
		id++;

	}*/

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
			System.out.println("Games"  + games.size());
			obuilder.add("games", abuilder);
			
			
			try {
				session.getBasicRemote().sendText(obuilder.build().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (input.getString("type").equals("CREATE")) {
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			System.out.println(message);
			Game g = new Game(id++, input.getString("game"), playerSessions.get(session), input.getString("player"));
			games.add(g);
			obuilder.add("type", "LOBBY");
			obuilder.add("gameId", g.getId());
			obuilder.add("gameName", g.getName());
			obuilder.add("isHost", true);

			for (Player p : g.getPlayers()) {
				
				abuilder.add(p.getPlayerName());
			}
			obuilder.add("players", abuilder);
			
			// System.out.println(obuilder.build().toString());
			try {
				session.getBasicRemote().sendText(obuilder.build().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(input.getInt("suspect"));

		
		} else if (input.getString("type").equals("JOIN")) {
			JsonObjectBuilder obuilder = Json.createObjectBuilder();
			JsonArrayBuilder abuilder = Json.createArrayBuilder();
			Game g = games.get(Integer.parseInt(input.getString("game")));
			g.addPlayer(playerSessions.get(session), input.getString("player"));
			obuilder.add("type", "LOBBY");
			obuilder.add("gameId", g.getId());
			obuilder.add("gameName", g.getName());
			obuilder.add("isHost", false);

			for (Player p : g.getPlayers()) {
				
				abuilder.add(p.getPlayerName());
			}
			obuilder.add("players", abuilder);
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
			boolean resetDebt = input.getBoolean("reset");
			Vector<Response> responses = g.startGame(resetDebt);
			sendResponses(responses, g);

		} else if (input.getString("type").equals("PASS")) {
			// Send 'game' with each msg.
			Vector<Response> responses;
			Game g = games.get(input.getInt("game"));
			responses = g.currentPlayerPass();

			
			sendResponses(responses, g);

		
		}else if (input.getString("type").equals("PLAY")) {
			// Send 'game' with each msg.
			Vector<Response> responses;
			Game g = games.get(input.getInt("game"));
			responses = g.currentPlayerPlay(input.getInt("bet"));

		
			sendResponses(responses, g);

		
		}else if (input.getString("type").equals("ACE_CHOICE")) {
			// Send 'game' with each msg.
			Vector<Response> responses;
			Game g = games.get(input.getInt("game"));
			responses = g.setAceAndFinishDeal(input.getString("isHigh"));
			
			sendResponses(responses, g);

		
		}else {
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
				if(res.getMsgs().containsKey("playerCard")){	
					System.out.println("Player Played need to pause");
					try {
						Thread.sleep(3500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				sendToSpecificPlayer(res);
			}
		}
		

	}
}
