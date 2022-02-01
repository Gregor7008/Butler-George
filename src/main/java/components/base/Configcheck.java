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

public class Configcheck {
	
	public static Configcheck INSTANCE;
	
	public Configcheck() {
		INSTANCE = this;
	}
	
	public void checkGuildConfigs(Guild guild) {
		String id = Configloader.INSTANCE.getGuildConfig(guild, "join2create");
		if (!id.equals("")) {
			VoiceChannel vc = guild.getVoiceChannelById(id);
			if (vc == null) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "join2create", id);
			} else {
				vc.putPermissionOverride(guild.getPublicRole()).setAllow(Permission.VIEW_CHANNEL).queue();
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "supporttalk");
		if (!id.equals("")) {
			if (guild.getVoiceChannelById(id) == null) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "supporttalk", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "supportchat");
		if (!id.equals("")) {
			TextChannel tc = guild.getTextChannelById(id);
			if (tc == null) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "supportchat", id);
			} else {
				tc.putPermissionOverride(guild.getPublicRole()).setAllow(Permission.VIEW_CHANNEL).queue();
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "ignored");
		if (!id.equals("")) {
			String[] entries = id.split(";");
			for (int a = 0; a < entries.length; a++) {
				TextChannel tc = guild.getTextChannelById(entries[a]);
				if (tc == null) {
					Configloader.INSTANCE.deleteGuildConfig(guild, "ignored", entries[a]);
				} else {
					tc.putPermissionOverride(guild.getPublicRole()).setAllow(Permission.VIEW_CHANNEL).queue();
				}
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "supportrole");
		if (!id.equals("")) {
			if (guild.getRoleById(id) == null ) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "supportrole", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "modrole");
		if (!id.equals("")) {
			if (guild.getRoleById(id) == null ) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "modrole", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "muterole");
		if (!id.equals("")) {
			if (guild.getRoleById(id) == null ) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "muterole", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "ccrole");
		if (!id.equals("")) {
			if (guild.getRoleById(id) == null ) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "ccrole", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess");
		if (!id.equals("")) {
			String[] entries = id.split(";");
			for (int a = 0; a < entries.length; a++) {
				if (guild.getRoleById(entries[a]) == null ) {
					Configloader.INSTANCE.deleteGuildConfig(guild, "ccdefaccess", entries[a]);
				}
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "reportchannel");
		if (!id.equals("")) {
			if (guild.getTextChannelById(id) == null) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "reportchannel", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "supportcategory");
		if (!id.equals("")) {
			if (guild.getCategoryById(id) == null) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "supportcategory", id);
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "levelrewards");
		if (!id.equals("")) {
			String[] entries = id.split(";");
			for (int a = 0; a < entries.length; a++) {
				String[] details = entries[a].split("_");
				if (guild.getRoleById(details[0]) == null) {
					Configloader.INSTANCE.deleteGuildConfig(guild, "levelrewards", entries[a]);
				}
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "welcomemsg");
		if (!id.equals("")) {
			String[] details = id.split(";");
			if (guild.getTextChannelById(details[1]) == null) {
				Configloader.INSTANCE.setGuildConfig(guild, "welcomemsg", "");
			}
		}
		id = Configloader.INSTANCE.getGuildConfig(guild, "goodbyemsg");
		if (!id.equals("")) {
			String[] details = id.split(";");
			if (guild.getTextChannelById(details[1]) == null) {
				Configloader.INSTANCE.setGuildConfig(guild, "goodbyemsg", "");
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
    				String id = Configloader.INSTANCE.getUserConfig(guild, user, "cccategory");
    	    		if (!id.equals("")) {
    	    			if (guild.getCategoryById(id) == null) {
    	    				Configloader.INSTANCE.deleteUserConfig(guild, user, "cccategory", id);
    	    			}
    	    		}
    			}
    		}
		}
	}
}
