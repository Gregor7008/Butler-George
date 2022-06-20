package components.actions;

import java.io.File;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public abstract class IOptionHolder {
	
	protected Object[] options = null;

	public Guild getOptionAsGuild(int index) {
		return (Guild) this.options[index];
	}
	
	public User getOptionAsUser(int index) {
		return (User) this.options[index];
	}
	
	public Role getOptionAsRole(int index) {
		return (Role) this.options[index];
	}
	
	public GuildChannel getOptionAsChannel(int index) {
		return (GuildChannel) this.options[index];
	}
	
	public String getOptionAsString(int index) {
		return (String) this.options[index];
	}
	
	public int getOptionAsInt(int index) {
		return (int) this.options[index];
	}
	
	public long getOptionAsLong(int index) {
		return (long) this.options[index];
	}
	
	public boolean getOptionAsBoolean(int index) {
		return (boolean) this.options[index];
	}
	
	public File getOptionAsFile(int index) {
		return (File) this.options[index];
	}
}