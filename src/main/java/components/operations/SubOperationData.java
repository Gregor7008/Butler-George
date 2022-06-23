package components.operations;

public class SubOperationData {

	private String name = null;
	private String info = null;
	
	public SubOperationData(String name, String info) {
		this.name = name;
		this.info = info;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInfo() {
		return this.info;
	}
	
	public boolean equals(String name) {
		return name.equalsIgnoreCase(this.name);
	}
}