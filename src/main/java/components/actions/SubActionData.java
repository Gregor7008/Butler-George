package components.actions;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SubActionData {

	private String name = null;
	private String info = null;
	private OptionType[] optionTypes = null;

	public SubActionData(String name, String info, OptionType[] optionTypes) {
		this.name = name;
		this.info = info;
		this.optionTypes = optionTypes;
	}
	
	public SubActionData(String name, String info, OptionType optionType) {
		this.name = name;
		this.info = info;
		this.optionTypes = new OptionType[] {optionType};
	}
	
	public SubActionData(String name, OptionType[] optionTypes) {
		this.name = name;
		this.optionTypes = optionTypes;
	}
	
	public SubActionData(String name, OptionType optionType) {
		this.name = name;
		this.optionTypes = new OptionType[] {optionType};
	}
	
	public SubActionData(String name) {
		this.name = name;
	}
	
	public SubActionData setName(String name) {
		this.name = name;
		return this;
	}
	
	public SubActionData setInfo(String info) {
		this.info = info;
		return this;
	}
	
	public SubActionData setOptionTypes(OptionType[] optionTypes) {
		this.optionTypes = optionTypes;
		return this;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInfo() {
		return this.info;	
	}
	
	public OptionType[] getOptionTypes() {
		return this.optionTypes;
	}
	
	public OptionType getOptionTypeAt(int index) {
		return this.optionTypes[index];
	}
}