package components.base;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class ConfigCheck {
	
	public static ConfigCheck INSTANCE;
	
	public ConfigCheck() {
		INSTANCE = this;
	}
	
	public void checkGuildConfigs(Guild guild) {
		//JSONObject configs
		JSONObject join2createchannels = ConfigLoader.run.getGuildConfig(guild).getJSONObject("join2createchannels");
		if (!join2createchannels.isEmpty()) {
			Set<String> channelids = join2createchannels.keySet();
			channelids.forEach(e -> {
				if (guild.getVoiceChannelById(e) == null) {
					join2createchannels.remove(e);
				}
			});
		}
		
		JSONObject levelrewards = ConfigLoader.run.getGuildConfig(guild).getJSONObject("levelrewards");
		if (!levelrewards.isEmpty()) {
			Set<String> levels = levelrewards.keySet();
			levels.forEach(e -> {
				if (guild.getRoleById(levelrewards.getLong(e)) == null) {
					levelrewards.remove(e);
				}
			});
		}
		//JSONArray configs
		JSONArray ccroles = ConfigLoader.run.getGuildConfig(guild).getJSONArray("customchannelroles");
		if (!ccroles.isEmpty()) {
			for (int i = 0; i < ccroles.length(); i++) {
				if (guild.getRoleById(ccroles.getLong(i)) == null) {
					ConfigLoader.run.removeValueFromArray(ccroles, i);
				}
			}
		}
		
		JSONArray ccaccessroles = ConfigLoader.run.getGuildConfig(guild).getJSONArray("customchannelaccessroles");
		if (!ccaccessroles.isEmpty()) {
			for (int i = 0; i < ccaccessroles.length(); i++) {
				if (guild.getRoleById(ccaccessroles.getLong(i)) == null) {
					ConfigLoader.run.removeValueFromArray(ccaccessroles, i);
				}
			}
		}
		//long configs
		long id = ConfigLoader.run.getGuildConfig(guild).getLong("supporttalk");
		if (id != 0) {
			if (guild.getVoiceChannelById(id) == null) {
				ConfigLoader.run.getGuildConfig(guild).put("supporttalk", Long.valueOf(0));
			}
		}
		
		id = ConfigLoader.run.getGuildConfig(guild).getLong("suggestionchannel");
		if (id != 0) {
			if (guild.getTextChannelById(id) == null) {
				ConfigLoader.run.getGuildConfig(guild).put("suggestionchannel", Long.valueOf(0));
			}
		}
		
		id = ConfigLoader.run.getGuildConfig(guild).getLong("supportchat");
		if (id != 0) {
			TextChannel tc = guild.getTextChannelById(id);
			if (tc == null) {
				ConfigLoader.run.getGuildConfig(guild).put("supportchat", Long.valueOf(0));
			} else {
				tc.upsertPermissionOverride(guild.getPublicRole()).setAllowed(Permission.VIEW_CHANNEL).queue();
			}
		}
		
		id = ConfigLoader.run.getGuildConfig(guild).getLong("reportchannel");
		if (id != 0) {
			if (guild.getTextChannelById(id) == null) {
				ConfigLoader.run.getGuildConfig(guild).put("reportchannel", Long.valueOf(0));
			}
		}
		
		id = ConfigLoader.run.getGuildConfig(guild).getLong("supportcategory");
		if (id != 0) {
			if (guild.getCategoryById(id) == null) {
				ConfigLoader.run.getGuildConfig(guild).put("supportcategory", Long.valueOf(0));
			}
		}
		//String configs
		String msg = ConfigLoader.run.getGuildConfig(guild).getString("welcomemsg");
		if (!msg.equals("")) {
			String[] details = msg.split(";");
			if (guild.getTextChannelById(details[1]) == null) {
				ConfigLoader.run.getGuildConfig(guild).put("welcomemsg", "");
			}
		}
		
		msg = ConfigLoader.run.getGuildConfig(guild).getString("goodbyemsg");
		if (!msg.equals("")) {
			String[] details = msg.split(";");
			if (guild.getTextChannelById(details[1]) == null) {
				ConfigLoader.run.getGuildConfig(guild).put("goodbyemsg", "");
			}
		}
	}
	
	public void checkUserConfigs(Guild guild) {
		//TODO Running through all users, checking whether id-specific values are still valid
	}
}
