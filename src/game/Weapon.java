package game;

public class Weapon extends GameComponent{

	private Location currentLocation;

	public Weapon(int i, String s, String n){
		super(i,s, n);
	}

	public void setLocation(Location locationById) {
		currentLocation = locationById;
	}
	public Location getLocation(){
		return currentLocation;
	}



}
