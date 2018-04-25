package game; 

public class Suspect extends GameComponent{

	private Location currentLocation;
	private boolean movedBySuggestion = false;

	
	public Suspect(int i, String s, String n){
		super(i,s, n);
	}
	public void setLocationBySuggestion(Location l) {
		currentLocation = l;
		//This should always be true, but just in case
		if(l.isRoom()){
			movedBySuggestion = true;
		}
	}
	public void setLocation(Location l) {
		currentLocation = l;
		movedBySuggestion = false;
	}
	
	public Location getLocation(){
		return currentLocation;
	}
	
	public boolean wasMovedBySuggestion(){
		return movedBySuggestion;
	}
	public void setMovedBySuggestion(boolean b) {
		movedBySuggestion = b;
	}

	

}
