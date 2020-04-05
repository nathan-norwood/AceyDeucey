package game;

import java.util.Vector;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class Player {
	private int unique_id;
	private String playerName;
	private boolean active = false;
	private double owedToPot;
	private double net;
	private Game game;
	
	public Player (int id, String playerName,Game game){
		unique_id = id;
		this.playerName = playerName;
		active = true;
		owedToPot = 0;
		this.game = game;
	}
	
	

	
	public int getUniqueId(){
		return unique_id;
	}
	public int getSuspectId(){
		return 0;
	}
	
	public String getPlayerName(){
		return playerName;
	}
	public boolean isActive(){
		return active;
	}
	public void setInactive(){
		active = false;
	}
	public void clearNet(){
		net = 0;
	}
	public void clearOwedToPot(){
		owedToPot = 0;
		
	}

	public void addToPot(double amount){
		owedToPot+=amount;
		net-=amount;
		game.addToPot(amount);
	}
	
	public void takeFromPot(double amount){
		owedToPot = owedToPot - amount;
		if(owedToPot <=0){
			owedToPot = 0;
		}
		net+=amount;
		game.subtractFromPot(amount);
	}
	
	public Response getDebt(){
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", "UPDATE_DEBT");
		builder.add("debt", getAmountOwed());
		builder.add("net", net);
		return new Response(getUniqueId(),builder.build());
	}
	public double getAmountOwed(){
		return owedToPot;
	}
	
}
