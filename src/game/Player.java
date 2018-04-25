package game;

import java.util.Vector;

public class Player {
	private int unique_id;
	private int suspect_id; /*TODO Currently established in GameBoard*/
	private boolean active = false;
	
	private Vector<Card> cards;
	
	public Player (int id, int s_id){
		unique_id = id;
		suspect_id=s_id;
		active = true;
		
		cards = new Vector<Card>();
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
		return suspect_id;
	}
	public boolean isActive(){
		return active;
	}
	public void setInactive(){
		active = false;
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
