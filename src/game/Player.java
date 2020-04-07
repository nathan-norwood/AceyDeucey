package game;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Player {
	private final int unique_id;
	private String playerName;
	private boolean active = false;
	private double owedToPot;
	
	private double net;
	private Game game;
	
	private Map<Player, Integer> onGoingDebts;
	
	public Player (int id, String playerName,Game game){
		unique_id = id;
		this.playerName = playerName;
		active = true;
		owedToPot = 0;
		this.game = game;
		this.onGoingDebts = new LinkedHashMap<Player,Integer>();
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
	
	public int getNet(){
		return (int) net;
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
	
	public Response getOnGoingDebts(){
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("type", "ONGOING_DEBTS");
		StringBuilder sBuilder = new StringBuilder();
		onGoingDebts.entrySet().stream()
		.forEach(entry -> sBuilder.append(getDebtRow(entry)));
		
		builder.add("debts", sBuilder.toString());
		return new Response(getUniqueId(), builder.build());
	}
	
	private String getDebtRow(Entry<Player,Integer> entry){
		String playerName = entry.getKey().getPlayerName();
		Integer p2pNet =  entry.getValue();
		String netHtml = p2pNet.toString();
		if(p2pNet >0){
			netHtml = "+"+netHtml;
		}
		return playerName + ": " +  netHtml +" ";
	}
	
	public void resetDebts(Vector<Player> players){
		for(Player p: players){
			if(!p.equals(this))
				onGoingDebts.put(p, 0);
		}
	}
	
	public void payPlayer(Player otherPlayer, int amount){
		adjustNet(amount);
		int currentNetWithPlayer = this.onGoingDebts.get(otherPlayer);
		currentNetWithPlayer = currentNetWithPlayer - amount;
		this.onGoingDebts.put(otherPlayer, currentNetWithPlayer);
		otherPlayer.receivePaymentFromPlayer(this, amount);
		
		
		
	}
	
	public void receivePaymentFromPlayer(Player otherPlayer, int amount){
		adjustNet(-1 * amount);
		
		int currentNetWithPlayer = this.onGoingDebts.get(otherPlayer);
		currentNetWithPlayer = currentNetWithPlayer + amount;
		this.onGoingDebts.put(otherPlayer, currentNetWithPlayer);
	}
	
	public void adjustNet(int amount){
		this.net = net+amount;
	}
	public String toString(){
		return playerName +": " + net;
	}
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((playerName == null) ? 0 : playerName.hashCode());
		result = prime * result + unique_id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (playerName == null) {
			if (other.playerName != null)
				return false;
		} else if (!playerName.equals(other.playerName))
			return false;
		if (unique_id != other.unique_id)
			return false;
		return true;
	}
}
