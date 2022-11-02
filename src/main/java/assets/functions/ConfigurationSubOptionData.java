package assets.functions;

public class ConfigurationSubOptionData {

	private String name = null;
	private String info = null;
	
	public ConfigurationSubOptionData(String name, String info) {
		this.name = name;
		this.info = info;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInfo() {
		return this.info;
	}
}