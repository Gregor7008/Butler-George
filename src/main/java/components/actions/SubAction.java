package components.actions;

import javax.annotation.Nullable;

public class SubAction extends IOptionHolder {
	
	private String name = null;
	
	public SubAction(String name, @Nullable Object[] options) {
		this.name = name;
		this.options = options;
	}
	
	public SubAction(String name, @Nullable Object option) {
		this.name = name;
		this.options = new Object[] {option};
	}
	
	public String getName() {
		return this.name;
	}
}