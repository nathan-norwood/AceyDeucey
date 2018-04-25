package game;

import java.util.Vector;

public class Location extends GameComponent{
	private Vector<Location> reachableLocations;
	private boolean occupied = false;
	private boolean isRoom = false;
	
	public Location(int i, String s, String n){
		super(i,s, n);
		reachableLocations = new Vector<Location>();
	}

	public void setIsRoom(){
		isRoom = true;
	}
	public void setOccupied(boolean state){
		occupied = state;
	}
	
	public void addLocation(Location l){
		reachableLocations.add(l);
	}

	public boolean isRoom() {
		// TODO Auto-generated method stub
		return isRoom;
	}
	
	public boolean isOccupied(){
		return occupied;
	}
	public Vector<Location> getAvailableLocationMoves(){
		Vector<Location> options = new Vector<Location>();
		for(Location l: reachableLocations){
			if(l.isRoom){
				options.add(l);
			}
			else{
				//if it is an empty hallway
				if(!l.isOccupied()){
					options.add(l);
				}
			}
		}
		return options;
	}
	

}
