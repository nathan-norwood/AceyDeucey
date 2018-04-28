package game;

import java.util.Vector;

public class Player {
	private int unique_id;
	private String playerName;
	private boolean active = false;
	private double owedToPot;
	private Vector<Card> cards;
	private Game game;
	
	public Player (int id, String playerName,Game game){
		unique_id = id;
		this.playerName = playerName;
		active = true;
		owedToPot = 0;
		cards = new Vector<Card>();
		this.game = game;
	}
	
	

	public void addCard( Card c ){
		cards.add(c);
		
	}
	public Vector<Card> getCards() {
		return cards;
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

	public void addToPot(double amount){
		owedToPot+=amount;
		game.addToPot(amount);
	}
	
	public double getAmountOwed(){
		return owedToPot;
	}
	public Card getCardById(int id){
		for(Card c:cards){
			if(c.getId()==id){
				return c;
			}
		}
		return null;
		
	}
}
