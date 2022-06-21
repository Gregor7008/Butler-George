package components.utilities;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import components.base.ConfigLoader;
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
		this.autoRolesCheck(guild);
		this.botAutoRolesCheck(guild);
		this.createdChannelsCheck(guild);
		this.customChannelAccessRolesCheck(guild);
		this.customChannelCategoriesCheck(guild, false);
		this.customChannelRolesCheck(guild);
		this.goodbyeMsgCheck(guild);
		this.join2CreateChannelsCheck(guild);
		this.levelMsgChannelCheck(guild);
		this.levelrewardsCheck(guild);
		this.modMailsCheck(guild);
		//Modrole Check
		this.offlineMsgCheck(guild);
		this.penaltiesCheck(guild);
		this.pollsCheck(guild);
		this.reactionRolesCheck(guild);
		this.reportChannelCheck(guild);
		this.suggestionChannelCheck(guild);
		this.supportCategoryCheck(guild);
		this.supportChatCheck(guild);
		//Supportrole Check
		this.supportTalkCheck(guild);
		this.systeminfoChannelCheck(guild);
		this.ticketChannelsCheck(guild);
	}
	public void autoRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray autoroles = guildConfig.getJSONArray("autoroles");
		for (int i = 0; i < autoroles.length(); i++) {
			if (guild.getRoleById(autoroles.getLong(i)) == null) {
				autoroles.remove(i);
			}
		}
	}
	public void botAutoRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray botautoroles = guildConfig.getJSONArray("botautoroles");
		for (int i = 0; i < botautoroles.length(); i++) {
			if (guild.getRoleById(botautoroles.getLong(i)) == null) {
				botautoroles.remove(i);
			}
		}
	}
	public void customChannelCategoriesCheck(Guild guild, boolean userCalled) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONObject customchannelcategories = guildConfig.getJSONObject("customchannelcategories");
		customchannelcategories.keySet().forEach(e -> {
			if (guild.getCategoryById(e) == null) {
				this.customChannelCategoryCheck(guild, guild.getMemberById(customchannelcategories.getLong(e)).getUser());
				if (!userCalled) {
					customchannelcategories.remove(e); 
				}
			} else if (guild.getMemberById(customchannelcategories.getLong(e)) == null || guild.getCategoryById(e).getChannels().size() == 0) {
				Category ctg = guild.getCategoryById(e);
				List<GuildChannel> channels = ctg.getChannels();
				for (int i = 0; i < channels.size(); i++) {
					channels.get(i).delete().queue();
				}
				ctg.delete().queue();
				if (!userCalled) {
					this.customChannelCategoryCheck(guild, guild.getMemberById(customchannelcategories.getLong(e)).getUser());
				}
				customchannelcategories.remove(e);
			}
		});
	}
	public void customChannelAccessRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray customchannelaccessroles = guildConfig.getJSONArray("customchannelaccessroles");
		for (int i = 0; i < customchannelaccessroles.length(); i++) {
			if (guild.getRoleById(customchannelaccessroles.getLong(i)) == null) {
				customchannelaccessroles.remove(i);
			}
		}
	}
	public void customChannelRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray customchannelroles = guildConfig.getJSONArray("customchannelroles");
		for (int i = 0; i < customchannelroles.length(); i++) {
			if (guild.getRoleById(customchannelroles.getLong(i)) == null) {
				customchannelroles.remove(i);
			}
		}
	}
	public void goodbyeMsgCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		String goodbyemsg = guildConfig.getString("goodbyemsg");
		if (goodbyemsg != "") {
			String[] goodbyeSplit = goodbyemsg.split(";");
			if (guild.getTextChannelById(goodbyeSplit[1]) == null) {
				guildConfig.put("goodbyemsg", "");
			}
		}
	}
	public void createdChannelsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
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
	}
	public void join2CreateChannelsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONObject join2createchannels = guildConfig.getJSONObject("join2createchannels");
		join2createchannels.keySet().forEach(e -> {
			if (guild.getVoiceChannelById(e) == null) {
				join2createchannels.remove(e);
			}
		});
	}
	public void levelMsgChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long levelmsgchannel = guildConfig.getLong("levelmsgchannel");
		if (guild.getTextChannelById(levelmsgchannel) == null) {
			guildConfig.put("levelmsgchannel", 0L);
		}
	}
	public void levelrewardsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONObject levelrewards = guildConfig.getJSONObject("levelrewards");
		levelrewards.keySet().forEach(e -> {
			if (guild.getRoleById(levelrewards.getLong(e)) == null) {
				levelrewards.remove(e);
			}
		});
	}
	public void offlineMsgCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long offlinemsg = guildConfig.getLong("offlinemsg");
		if (offlinemsg != 0) {
			if (guildConfig.getLong("systeminfochannel") != 0) {
				if (guild.getTextChannelById(guildConfig.getLong("systeminfochannel")).retrieveMessageById(offlinemsg).complete() == null) {
					guildConfig.put("offlinemsg", 0L);
				}
			} else {
				guildConfig.put("offlinemsg", 0L);
			}
		}
	}
	public void penaltiesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONObject penalties = guildConfig.getJSONObject("penalties");
		penalties.keySet().forEach(e -> {
			JSONArray penalty = penalties.getJSONArray(e);
			if (penalty.getString(0).equals("rr") && guild.getRoleById(penalty.getString(1)) == null) {
				penalties.remove(e);
			}
		});
	}
	public void reportChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long reportchannel = guildConfig.getLong("reportchannel");
		if (guild.getTextChannelById(reportchannel) == null) {
			guildConfig.put("reportchannel", 0L);
		}
	}
	public void suggestionChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long suggestionchannel = guildConfig.getLong("suggestionchannel");
		if (guild.getTextChannelById(suggestionchannel) == null) {
			guildConfig.put("suggestionchannel", 0L);
		}
	}
	public void supportCategoryCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long supportcategory = guildConfig.getLong("supportcategory");
		if (guild.getCategoryById(supportcategory) == null) {
			guildConfig.put("supportcategory", 0L);
		}
	}
	public void supportChatCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long supportchat = guildConfig.getLong("supportchat");
		if (guild.getTextChannelById(supportchat) == null) {
			guildConfig.put("supportchat", 0L);
		}
	}
	public void supportTalkCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long supporttalk = guildConfig.getLong("supporttalk");
		if (guild.getVoiceChannelById(supporttalk) == null) {
			guildConfig.put("supporttalk", 0L);
		}
	}
	public void systeminfoChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long systeminfochannel = guildConfig.getLong("systeminfochannel");
		if (guild.getTextChannelById(systeminfochannel) == null) {
			guildConfig.put("systeminfochannel", 0L);
		}
	}
	public void ticketChannelsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray ticketchannels = guildConfig.getJSONArray("ticketchannels");
		for (int i = 0; i < ticketchannels.length(); i++) {
			if (guild.getTextChannelById(ticketchannels.getLong(i)) == null) {
				ticketchannels.remove(i);
			}
		}
	}
	public void welcomeMsgCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		String welcomemsg = guildConfig.getString("welcomemsg");
		if (welcomemsg != "") {
			String[] welcomeSplit = welcomemsg.split(";");
			if (guild.getTextChannelById(welcomeSplit[1]) == null) {
				guildConfig.put("welcomemsg", "");
			}
		}
	}
	public void modMailsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONObject modmails = guildConfig.getJSONObject("modmails");
		modmails.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null) {
				modmails.remove(e);
			}
		});
	}
	public void pollsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
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
	}
	public void reactionRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
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
		this.customChannelCategoryCheck(guild, user);
	}
	
	public void customChannelCategoryCheck(Guild guild, User user) {
		JSONObject userConfig = ConfigLoader.getMemberConfig(guild, user);
		long customchannelcategory = userConfig.getLong("customchannelcategory");
		if (guild.getCategoryById(customchannelcategory) == null) {
			userConfig.put("customchannelcategory", 0L);
			this.customChannelCategoriesCheck(guild, true);
		}
	}
}
