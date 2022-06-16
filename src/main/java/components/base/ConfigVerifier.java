package components.base;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ConfigVerifier {
	
	public static ConfigVerifier run;
	
	public ConfigVerifier() {
		run = this;
	}
	
	public void guildCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		
		JSONArray autoroles = guildConfig.getJSONArray("autoroles");
		for (int i = 0; i < autoroles.length(); i++) {
			if (guild.getRoleById(autoroles.getLong(i)) == null) {
				autoroles.remove(i);
			}
		}
		
		JSONArray botautoroles = guildConfig.getJSONArray("botautoroles");
		for (int i = 0; i < botautoroles.length(); i++) {
			if (guild.getRoleById(botautoroles.getLong(i)) == null) {
				botautoroles.remove(i);
			}
		}
		
		JSONObject customchannelcategories = guildConfig.getJSONObject("customchannelcategories");
		customchannelcategories.keySet().forEach(e -> {
			if (guild.getCategoryById(e) == null) {
				customchannelcategories.remove(e);
			} else {
				if (guild.getMemberById(customchannelcategories.getLong(e)) == null) {
					Category ctg = guild.getCategoryById(e);
					List<GuildChannel> channels = ctg.getChannels();
					for (int i = 0; i < channels.size(); i++) {
						channels.get(i).delete().queue();
					}
					ctg.delete().queue();
				}
			}
		});
		
		JSONArray customchannelaccessroles = guildConfig.getJSONArray("customchannelaccessroles");
		for (int i = 0; i < customchannelaccessroles.length(); i++) {
			if (guild.getRoleById(customchannelaccessroles.getLong(i)) == null) {
				customchannelaccessroles.remove(i);
			}
		}
		
		JSONArray customchannelroles = guildConfig.getJSONArray("customchannelroles");
		for (int i = 0; i < customchannelroles.length(); i++) {
			if (guild.getRoleById(customchannelroles.getLong(i)) == null) {
				customchannelroles.remove(i);
			}
		}
		
		String goodbyemsg = guildConfig.getString("goodbyemsg");
		String[] goodbyeSplit = goodbyemsg.split(";");
		if (guild.getTextChannelById(goodbyeSplit[1]) == null) {
			guildConfig.put("goodbyemsg", "");
		}
		
		JSONObject createdchannels = guildConfig.getJSONObject("createdchannels");
		createdchannels.keySet().forEach(e -> {
			if (guild.getVoiceChannelById(e) == null) {
				createdchannels.remove(e);
			} else if (guild.getVoiceChannelById(e).getMembers().size() == 0) {
				guild.getVoiceChannelById(e).delete().queue();
				createdchannels.remove(e);
			} else {
				if (guild.getMemberById(createdchannels.getLong(e)) == null) {
					guild.getVoiceChannelById(e).delete().queue();
					createdchannels.remove(e);
				}
			}
		});
		
		JSONObject join2createchannels = guildConfig.getJSONObject("join2createchannels");
		join2createchannels.keySet().forEach(e -> {
			if (guild.getVoiceChannelById(e) == null) {
				join2createchannels.remove(e);
			}
		});
		
		long levelmsgchannel = guildConfig.getLong("levelmsgchannel");
		if (guild.getTextChannelById(levelmsgchannel) == null) {
			guildConfig.put("levelmsgchannel", 0L);
		}
		
		JSONObject levelrewards = guildConfig.getJSONObject("levelrewards");
		levelrewards.keySet().forEach(e -> {
			if (guild.getRoleById(levelrewards.getLong(e)) == null) {
				levelrewards.remove(e);
			}
		});
		
		String offlinemsg = guildConfig.getString("offlinemsg");
		String[] offlineSplit = offlinemsg.split(";");
		if (guild.getTextChannelById(offlineSplit[1]) == null) {
			guildConfig.put("offlinemsg", "");
		}
		
		JSONObject penalties = guildConfig.getJSONObject("penalties");
		penalties.keySet().forEach(e -> {
			JSONArray penalty = penalties.getJSONArray(e);
			if (penalty.getString(0).equals("rr") && guild.getRoleById(penalty.getString(1)) == null) {
				penalties.remove(e);
			}
		});
		
		long reportchannel = guildConfig.getLong("reportchannel");
		if (guild.getTextChannelById(reportchannel) == null) {
			guildConfig.put("reportchannel", 0L);
		}
		
		long suggestionchannel = guildConfig.getLong("suggestionchannel");
		if (guild.getTextChannelById(suggestionchannel) == null) {
			guildConfig.put("suggestionchannel", 0L);
		}
		
		long supportcategory = guildConfig.getLong("supportcategory");
		if (guild.getCategoryById(supportcategory) == null) {
			guildConfig.put("supportcategory", 0L);
		}
		
		long supportchat = guildConfig.getLong("supportchat");
		if (guild.getTextChannelById(supportchat) == null) {
			guildConfig.put("supportchat", 0L);
		}
		
		long supporttalk = guildConfig.getLong("supporttalk");
		if (guild.getVoiceChannelById(supporttalk) == null) {
			guildConfig.put("supporttalk", 0L);
		}
		
		JSONArray ticketchannels = guildConfig.getJSONArray("ticketchannels");
		for (int i = 0; i < ticketchannels.length(); i++) {
			if (guild.getTextChannelById(ticketchannels.getLong(i)) == null) {
				ticketchannels.remove(i);
			}
		}
		
		String welcomemsg = guildConfig.getString("welcomemsg");
		String[] welcomeSplit = welcomemsg.split(";");
		if (guild.getTextChannelById(welcomeSplit[1]) == null) {
			guildConfig.put("welcomemsg", "");
		}
		
		JSONObject modmails = guildConfig.getJSONObject("modmails");
		modmails.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null) {
				modmails.remove(e);
			}
		});
		
		JSONObject polls = guildConfig.getJSONObject("polls");
		polls.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null || polls.getJSONObject(e).isEmpty()) {
				polls.remove(e);
			} else {
				JSONObject channelConfig = polls.getJSONObject(e);
				channelConfig.keySet().forEach(o -> {
					if (guild.getTextChannelById(e).retrieveMessageById(o) == null || channelConfig.getJSONObject(o).isEmpty()) {
						channelConfig.remove(o);
					}
				});
				if (polls.getJSONObject(e).isEmpty()) {
					polls.remove(e);
				}
			}
		});
		
		JSONObject reactionroles = guildConfig.getJSONObject("reactionroles");
		reactionroles.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null || reactionroles.getJSONObject(e).isEmpty()) {
				reactionroles.remove(e);
			} else {
				JSONObject channelConfig = reactionroles.getJSONObject(e);
				channelConfig.keySet().forEach(o -> {
					if (guild.getTextChannelById(e).retrieveMessageById(o) == null || channelConfig.getJSONObject(o).isEmpty()) {
						channelConfig.remove(o);
					}
				});
				if (reactionroles.getJSONObject(e).isEmpty()) {
					reactionroles.remove(e);
				}
			}
		});
	}
	
	public void usersCheck(Guild guild) {
		new Thread(() -> {
			List<Member> members = guild.loadMembers().get();
			for (int i = 0; i < members.size(); i++) {
				this.userCheck(guild, members.get(i).getUser());
			}
		}).start();
	}
	
	public void userCheck(Guild guild, User user) {
		JSONObject userConfig = ConfigLoader.getMemberConfig(guild, user);
		
		long customchannelcategory = userConfig.getLong("customchannelcategory");
		if (guild.getCategoryById(customchannelcategory) == null) {
			userConfig.put("customchannelcategory", 0L);
		}
	}
}
