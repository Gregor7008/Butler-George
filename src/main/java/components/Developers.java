package components;

import java.util.ArrayList;
import java.util.List;

public class Developers {
	
	public static Developers INSTANCE;
	public List<String> developers = new ArrayList<String>();
	
	public Developers() {
		developers.add("475974084937646080");
		developers.add("806631059667025940");
		developers.add("407547342628323338");
	}
	
	public static Developers getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Developers();
		}
		return INSTANCE;
	}
	
	public List<String> getDevelopers() {
		return developers;
	}

}
