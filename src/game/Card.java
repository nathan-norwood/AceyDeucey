package game;

/* It seems like it would be useful to keep the friendly names
 * for each of these in the GamePieces object, and use an ID to 
 * reference them... this way the same suspect ID used here to 
 * reference the name of the suspect could be used in Player to
 * reference the suspect being played... 
 */

public class Card {
	private Suit suit;
	private CardValue cardValue;
	private int id;
	
	
	public Card(int i, Suit s, CardValue cardValue  ){
		setId(i);
		setSuit(s);
		setCardValue(cardValue);
	}

	public String getImagePath(){
		
		String valStr = (cardValue.getCardValue()<=10)? Integer.toString(cardValue.getCardValue()) : "" + cardValue.name().charAt(0);
		String suitStr = "" + suit.name().charAt(0);
		
		return "images/cards/"+valStr+suitStr+".png";
		
	}

	private void setCardValue(CardValue cardValue) {
		this.cardValue = cardValue;
		
	}



	private void setSuit(Suit suit) {
		this.suit = suit;
	}

	public int getId() {
		return id;
	}

	private void setId(int unique_id) {
		this.id = unique_id;
	}

	public String getName() {
		return cardValue.name() + " of " +  suit.name();
	}

	public CardValue getCardValue(){
		return cardValue;
	}
	

}
