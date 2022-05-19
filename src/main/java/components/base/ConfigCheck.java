package components.base;

import java.io.File;
import java.util.List;

import base.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class ConfigCheck {
	
	public static ConfigCheck INSTANCE;
	
	public ConfigCheck() {
		INSTANCE = this;
	}
	
	public void checkGuildConfigs(Guild guild) {
		String id = ConfigLoader.cfl.getGuildConfig(guild, "join2create");
		if (!id.equals("")) {
			String[] entries = id.split(";");
			for (int i = 0; i < entries.length; i++) {
				VoiceChannel vc = guild.getVoiceChannelById(entries[i]);
				if (vc == null) {
					ConfigLoader.cfl.removeGuildConfig(guild, "join2create", entries[i]);
				}
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "supporttalk");
		if (!id.equals("")) {
			if (guild.getVoiceChannelById(id) == null) {
				ConfigLoader.cfl.removeGuildConfig(guild, "supporttalk", id);
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "supportchat");
		if (!id.equals("")) {
			TextChannel tc = guild.getTextChannelById(id);
			if (tc == null) {
				ConfigLoader.cfl.removeGuildConfig(guild, "supportchat", id);
			} else {
				tc.upsertPermissionOverride(guild.getPublicRole()).setAllowed(Permission.VIEW_CHANNEL).queue();
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "ccrole");
		if (!id.equals("")) {
			if (guild.getRoleById(id) == null ) {
				ConfigLoader.cfl.removeGuildConfig(guild, "ccrole", id);
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "ccdefaccess");
		if (!id.equals("")) {
			String[] entries = id.split(";");
			for (int a = 0; a < entries.length; a++) {
				if (guild.getRoleById(entries[a]) == null ) {
					ConfigLoader.cfl.removeGuildConfig(guild, "ccdefaccess", entries[a]);
				}
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "reportchannel");
		if (!id.equals("")) {
			if (guild.getTextChannelById(id) == null) {
				ConfigLoader.cfl.removeGuildConfig(guild, "reportchannel", id);
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "supportcategory");
		if (!id.equals("")) {
			if (guild.getCategoryById(id) == null) {
				ConfigLoader.cfl.removeGuildConfig(guild, "supportcategory", id);
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "levelrewards");
		if (!id.equals("")) {
			String[] entries = id.split(";");
			for (int a = 0; a < entries.length; a++) {
				String[] details = entries[a].split("_");
				if (guild.getRoleById(details[0]) == null) {
					ConfigLoader.cfl.removeGuildConfig(guild, "levelrewards", entries[a]);
				}
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "welcomemsg");
		if (!id.equals("")) {
			String[] details = id.split(";");
			if (guild.getTextChannelById(details[1]) == null) {
				ConfigLoader.cfl.setGuildConfig(guild, "welcomemsg", "");
			}
		}
		id = ConfigLoader.cfl.getGuildConfig(guild, "goodbyemsg");
		if (!id.equals("")) {
			String[] details = id.split(";");
			if (guild.getTextChannelById(details[1]) == null) {
				ConfigLoader.cfl.setGuildConfig(guild, "goodbyemsg", "");
			}
		}
	}
	
	public void checkUserConfigs(Guild guild) {
		File guilddir = new File(Bot.environment + "/configs/user/" + guild.getId());
		if (guilddir.exists()) {
			List<Member> members = guild.loadMembers().get();
    		for (int a = 0; a < members.size(); a++) {
    			User user = members.get(a).getUser();
    			File pFile = new File(Bot.environment + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
    			if (pFile.exists()) {
    				String id = ConfigLoader.cfl.getUserConfig(guild, user, "cccategory");
    	    		if (!id.equals("")) {
    	    			if (guild.getCategoryById(id) == null) {
    	    				ConfigLoader.cfl.removeUserConfig(guild, user, "cccategory", id);
    	    				ConfigLoader.cfl.removeGuildConfig(guild, "ccctgies", id);
    	    			}
    	    		}
    			}
    		}
		}
	}
}
