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
		this.userAutoRolesCheck(guild);
		this.botAutoRolesCheck(guild);
		this.createdChannelsCheck(guild);
		//Customchannelaccessroles Check
		this.customChannelCategoriesCheck(guild, false);
		this.customChannelRolesCheck(guild);
		this.goodbyeMsgCheck(guild);
		this.join2CreateChannelsCheck(guild);
		this.levelrewardsCheck(guild);
		this.modMailsCheck(guild);
		//Modrole Check
		this.penaltiesCheck(guild);
		this.pollsCheck(guild);
		this.reactionRolesCheck(guild);
		this.communityInboxChannelCheck(guild);
		this.suggestionInboxCheck(guild);
		//Supportrole Check
		this.supportTalkCheck(guild);
		this.modInboxChannelCheck(guild);
		this.ticketChannelsCheck(guild);
	}
	public void userAutoRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray autoroles = guildConfig.getJSONArray("userautoroles");
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
	public void communityInboxChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long communityInbox = guildConfig.getLong("communityinbox");
		if (guild.getTextChannelById(communityInbox) == null) {
			guildConfig.put("communityinbox", 0L);
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
	public void goodbyeMsgCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONArray goodbyemsg = guildConfig.getJSONArray("goodbyemsg");
		if (!goodbyemsg.isEmpty()) {
			if (guild.getTextChannelById(goodbyemsg.getLong(1)) == null) {
				guildConfig.put("goodbyemsg", new JSONArray());
			}
		}
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
	public void levelrewardsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		JSONObject levelrewards = guildConfig.getJSONObject("levelrewards");
		levelrewards.keySet().forEach(e -> {
			if (guild.getRoleById(levelrewards.getLong(e)) == null) {
				levelrewards.remove(e);
			}
		});
	}
	public void modRoleCheck(Guild guild) {
		
	}
	public void modInboxChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long modInbox = guildConfig.getLong("moderationinbox");
		if (guild.getTextChannelById(modInbox) == null) {
			guildConfig.put("moderationinbox", 0L);
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
	public void suggestionInboxCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long suggestionInbox = guildConfig.getLong("suggestioninbox");
		if (guild.getTextChannelById(suggestionInbox) == null) {
			guildConfig.put("suggestioninbox", 0L);
		}
	}
	public void supportRoleCheck(Guild guild) {
		
	}
	public void supportTalkCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.getGuildConfig(guild);
		long supporttalk = guildConfig.getLong("supporttalk");
		if (guild.getVoiceChannelById(supporttalk) == null) {
			guildConfig.put("supporttalk", 0L);
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
		JSONArray welcomemsg = guildConfig.getJSONArray("welcomemsg");
		if (!welcomemsg.isEmpty()) {
			if (guild.getTextChannelById(welcomemsg.getLong(1)) == null) {
				guildConfig.put("welcomemsg", new JSONArray());
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
				User user = members.get(i).getUser();
				if (!user.isBot()) {
					this.userCheck(guild, user);
				}
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
