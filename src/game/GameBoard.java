package game;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import org.apache.logging.log4j.Logger;

/* TODO: Should we shift how we do IDs? so they can be shared with cards? */
public class GameBoard {

	private Vector<Suspect> suspects;
	private Vector<Weapon> weapons;
	private Vector<Location> locations;
	private Suspect scarlet;
	
	
	public GameBoard(){
		/* Create Game Components */
		locations = new Vector<Location>();
		suspects = new Vector<Suspect>();
		
		int id = 0;

		weapons = new Vector<Weapon>();
		weapons.add(new Weapon(id++, "Rope", "rope.png"));
		weapons.add(new Weapon(id++, "Lead Pipe", "leadpipe.png"));
		weapons.add(new Weapon(id++, "Knife", "knife.png"));
		weapons.add(new Weapon(id++, "Wrench","wrench.png"));
		weapons.add(new Weapon(id++, "Candlestick", "candlestick.png"));
		weapons.add(new Weapon(id++, "Revolver", "revolver.png"));
		
		setupBoard(id);
		
		/*TODO: Layout Game Components on board */
		/* Suspects go in defined rooms
		 * Weapons are laid out randomly
		 * 
		 */
		
	}
	
	public Suspect getSuspectById(int id) {

		for (Suspect s : suspects) {
			if (s.hasID(id)) {
				return s;
			}
		}
		return null;

	}

	public Location getLocationById(int id) {
		for (Location r : locations) {
			if (r.hasID(id)) {
				return r;
			}
		}
		return null;
	}

	public Weapon getWeaponById(int id) {
		for (Weapon w : weapons) {
			if (w.hasID(id)) {
				return w;
			}
		}

		return null;
	}
	
	public int getScarlet(){
		return scarlet.getId();
	}
	
	private void setupBoard(int i){
		
		/* TODO: I found it easier to use a single Location with a few flags
		 * to differentiate behavior between rooms/hallways - we can shift this 
		 * later as needed.
		 */
		
		/* TODO: somehow we will need to associate physical board with these
		 * locations - we can give them a position # or we can assign the board 
		 * IDs... either way.
		 */
		int id = i;
		Location study = new Location(id++, "Study", "study.png");
		study.setIsRoom();
		
		Location hall  = new Location(id++, "Hall", "hall.png");
		hall.setIsRoom();
		
		Location lounge = new Location(id++, "Lounge", "lounge.png");
		lounge.setIsRoom();
		
		Location library = new Location(id++, "Library", "library.png");
		library.setIsRoom();
		
		Location billiard = new Location(id++, "Billiard Room", "billiardroom.png");
		billiard.setIsRoom();
		
		Location dining = new Location(id++, "Dining Room", "diningroom.png");
		dining.setIsRoom();
		
		Location conservatory = new Location(id++, "Conservatory", "conservatory.png");
		conservatory.setIsRoom();
		
		Location ballroom = new Location(id++, "Ballroom", "ballroom.png");
		ballroom.setIsRoom();
		
		Location kitchen = new Location(id++, "Kitchen", "kitchen.png");
		kitchen.setIsRoom();
		
		Location h1 = new Location(id++, "H1", "");
		Location h2 = new Location(id++, "H2", "");
		Location h3 = new Location(id++, "H3", "");
		Location h4 = new Location(id++, "H4", "");
		Location h5 = new Location(id++, "H5", "");
		Location h6 = new Location(id++, "H6", "");
		Location h7 = new Location(id++, "H7", "");
		Location h8 = new Location(id++, "H8", "");
		Location h9 = new Location(id++, "H9", "");
		Location h10 = new Location(id++, "H10", "");
		Location h11 = new Location(id++, "H11", "");
		Location h12 = new Location(id++, "H12", "");
		
		study.addLocation(h1);
		study.addLocation(h3);
		study.addLocation(kitchen);  //TODO: Handle secret passageways differently?
		locations.add(study);
		
		h1.addLocation(study);
		h1.addLocation(hall);
		locations.add(h1);
		
		hall.addLocation(h1);
		hall.addLocation(h2);
		hall.addLocation(h4);
		locations.add(hall);
		
		h2.addLocation(hall);
		h2.addLocation(lounge);
		locations.add(h2);
		
		lounge.addLocation(h2);
		lounge.addLocation(h5);
		lounge.addLocation(conservatory);
		locations.add(lounge);
		
		h3.addLocation(study);
		h3.addLocation(library);
		locations.add(h3);
		
		h4.addLocation(hall);
		h4.addLocation(billiard);
		locations.add(h4);
		
		h5.addLocation(lounge);
		h5.addLocation(dining);
		locations.add(h5);
		
		library.addLocation(h3);
		library.addLocation(h6);
		library.addLocation(h8);
		locations.add(library);
		
		h6.addLocation(library);
		h6.addLocation(billiard);
		locations.add(h6);
		
		billiard.addLocation(h4);
		billiard.addLocation(h6);
		billiard.addLocation(h7);
		billiard.addLocation(h9);
		locations.add(billiard);
		
		h7.addLocation(billiard);
		h7.addLocation(dining);
		locations.add(h7);
		
		dining.addLocation(h5);
		dining.addLocation(h7);
		dining.addLocation(h10);
		locations.add(dining);
		
		h8.addLocation(library);
		h8.addLocation(conservatory);
		locations.add(h8);
		
		h9.addLocation(billiard);
		h9.addLocation(ballroom);
		locations.add(h9);
		
		h10.addLocation(dining);
		h10.addLocation(kitchen);
		locations.add(h10);
		
		conservatory.addLocation(h8);
		conservatory.addLocation(h11);
		conservatory.addLocation(lounge);
		locations.add(conservatory);
		
		h11.addLocation(conservatory);
		h11.addLocation(ballroom);
		locations.add(h11);

		ballroom.addLocation(h9);
		ballroom.addLocation(h11);
		ballroom.addLocation(h12);
		locations.add(ballroom);
		
		h12.addLocation(ballroom);
		h12.addLocation(kitchen);
		locations.add(h12);
		
		kitchen.addLocation(h10);
		kitchen.addLocation(h12);
		kitchen.addLocation(study);
		locations.add(kitchen);
		
		Suspect scarlet = new Suspect(id++, "Miss Scarlet", "scarlet.png"); 
		scarlet.setLocation(h2);
		suspects.add(scarlet);
		h2.setOccupied(true);
		this.scarlet = scarlet;
		
		Suspect mustard = new Suspect(id++, "Colonel Mustard", "mustard.png");
		mustard.setLocation(h5);
		suspects.add(mustard);
		h5.setOccupied(true);
		
		Suspect plum = new Suspect(id++, "Professor Plum", "plum.png");
		plum.setLocation(h3);
		suspects.add(plum);
		h3.setOccupied(true);
		
		Suspect green = new Suspect(id++, "Mr. Green", "green.png");
		green.setLocation(h11);
		suspects.add(green);
		h11.setOccupied(true);
		
		Suspect white = new Suspect(id++, "Mrs. White", "white.png");
		white.setLocation(h12);
		suspects.add(white);
		h12.setOccupied(true);
		
		Suspect peacock = new Suspect(id++, "Mrs. Peacock", "peacock.png");
		peacock.setLocation(h8);
		suspects.add(peacock);
		h8.setOccupied(true);
		
		putWeaponsInRooms();
		
	}
	
	private void putWeaponsInRooms() {

		Random rand = new Random();
		int next;
		for(Weapon w: weapons){
			do{
				next = rand.nextInt(locations.size());
				if(locations.get(next).isRoom()){
					w.setLocation(locations.get(next));
				}
			}while(w.getLocation()==null);
		}
		
	}

	public HashMap<Integer, String> getSuspects(){
		HashMap<Integer, String> list = new HashMap<Integer,String>();
		for(Suspect s:suspects){
			list.put(s.getId(), s.getName());
		}	
		return list;
	}
	public HashMap<Integer, String> getRooms(){
		HashMap<Integer, String> list = new HashMap<Integer,String>();
		for(Location l:locations){
			if(l.isRoom()){
				list.put(l.getId(), l.getName());
			}
		}	
		return list;
	}
	public HashMap<Integer, String> getWeapons(){
		HashMap<Integer, String> list = new HashMap<Integer,String>();
		for(Weapon w:weapons){
			list.put(w.getId(), w.getName());
		}	
		return list;
	}
	public Vector<Weapon> getWeaponSet(){
		return weapons;
	}
	public Vector<Location> getRoomSet(){
		Vector<Location> rooms = new Vector<Location>();
		for(Location l: locations){
			if(l.isRoom()){
				rooms.add(l);
			}
		}
		return rooms;
	}
	public Vector<Suspect> getSuspectSet(){
		return suspects;
	}
	
	public Vector<GameComponent> getGameComponenets(){
		Vector<GameComponent> gameComponents = new Vector<GameComponent>();
		for(Location l : locations){
				if(l.isRoom()){
					gameComponents.add(l);
				}
		}
		
		
		for(Suspect s: suspects){
			gameComponents.addElement(s);
		}
		
		for(Weapon w: weapons){
			gameComponents.add(w);
		}
		
		return gameComponents;
	}
}
