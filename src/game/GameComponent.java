package game;

public abstract class GameComponent {

	private int unique_id;
	private String name;
	private String img_name;
	
	public GameComponent(int i, String n, String in) {
		unique_id =i;
		name = n;
		img_name = in;
	}

	public boolean hasID(int id) {
		if(id == unique_id){
			return true;
		}
		return false;
	}
	
	public int getId(){
		return unique_id;
	}
	
	public String getName(){
		return name;
	}
	public String getImgName(){
		return img_name;
	}
}
