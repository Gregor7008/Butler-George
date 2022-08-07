package base.engines.configs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ConfigVerifier {
	
	public static ConfigVerifier RUN;
	
	public ConfigVerifier() {
		RUN = this;
	}
	
	public void guildCheck(Guild guild) {
		this.adminRoleCheck(guild);
		this.userAutoRolesCheck(guild);
		this.botAutoRolesCheck(guild);
		this.createdChannelsCheck(guild);
		//Customchannelaccessroles Check
		this.customChannelCategoriesCheck(guild, false);
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
	}
	public void adminRoleCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray adminroles = guildConfig.getJSONArray("adminroles");
		for (int i = 0; i < adminroles.length(); i++) {
			if (guild.getRoleById(adminroles.getLong(i)) == null) {
				adminroles.remove(i);
			}
		}
	}
	public void userAutoRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray autoroles = guildConfig.getJSONArray("userautoroles");
		for (int i = 0; i < autoroles.length(); i++) {
			if (guild.getRoleById(autoroles.getLong(i)) == null) {
				autoroles.remove(i);
			}
		}
	}
	public void botAutoRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray botautoroles = guildConfig.getJSONArray("botautoroles");
		for (int i = 0; i < botautoroles.length(); i++) {
			if (guild.getRoleById(botautoroles.getLong(i)) == null) {
				botautoroles.remove(i);
			}
		}
	}
	public void communityInboxChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		long communityInbox = guildConfig.getLong("communityinbox");
		if (guild.getTextChannelById(communityInbox) == null) {
			guildConfig.put("communityinbox", 0L);
		}
	}
	public void customChannelCategoriesCheck(Guild guild, boolean userCalled) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject customchannelcategories = guildConfig.getJSONObject("customchannelcategories");
		List<String> keysToRemove = new ArrayList<>();
		customchannelcategories.keySet().forEach(e -> {
			if (guild.getCategoryById(e) == null) {
				this.customChannelCategoryCheck(guild, guild.getMemberById(customchannelcategories.getLong(e)).getUser());
				if (!userCalled) {
					keysToRemove.add(e);
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
				keysToRemove.add(e);
			}
		});
		for (int i = 0; i < keysToRemove.size(); i++) {
			customchannelcategories.remove(keysToRemove.get(i));
		}
	}
	public void customChannelPolicingRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray customchannelpolicingroles = guildConfig.getJSONArray("customchannelpolicingroles");
		for (int i = 0; i < customchannelpolicingroles.length(); i++) {
			if (guild.getRoleById(customchannelpolicingroles.getLong(i)) == null) {
				customchannelpolicingroles.remove(i);
			}
		}
	}
	public void createdChannelsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject createdchannels = guildConfig.getJSONObject("createdchannels");
		List<String> keysToRemove1 = new ArrayList<>();
		createdchannels.keySet().forEach(e -> {
			JSONObject parent = createdchannels.getJSONObject(e);
			List<String> keysToRemove2 = new ArrayList<>();
			parent.keySet().forEach(a -> {
				if (guild.getVoiceChannelById(a) == null) {
					keysToRemove2.add(a);
				} else if (guild.getVoiceChannelById(a).getMembers().size() == 0) {
					guild.getVoiceChannelById(a).delete().queue();
					keysToRemove2.add(a);
				} else {
					if (guild.getMemberById(parent.getJSONArray(a).getLong(0)) == null) {
						guild.getVoiceChannelById(a).delete().queue();
						keysToRemove2.add(a);
					}
				}
			});
			for (int i = 0; i < keysToRemove2.size(); i++) {
				parent.remove(keysToRemove2.get(i));
			}
			if (parent.isEmpty()) {
				keysToRemove1.add(e);
			}
		});
		for (int i = 0; i < keysToRemove1.size(); i++) {
			createdchannels.remove(keysToRemove1.get(i));
		}
	}
	public void goodbyeMsgCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray goodbyemsg = guildConfig.getJSONArray("goodbyemsg");
		if (!goodbyemsg.isEmpty()) {
			if (guild.getTextChannelById(goodbyemsg.getLong(1)) == null) {
				guildConfig.put("goodbyemsg", new JSONArray());
			}
		}
	}
	public void join2CreateChannelsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject join2createchannels = guildConfig.getJSONObject("join2createchannels");
		List<String> keysToRemove = new ArrayList<>();
		join2createchannels.keySet().forEach(e -> {
			if (guild.getVoiceChannelById(e) == null) {
				keysToRemove.add(e);
			}
		});
		for (int i = 0; i < keysToRemove.size(); i++) {
			join2createchannels.remove(keysToRemove.get(i));
		}
	}
	public void levelrewardsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject levelrewards = guildConfig.getJSONObject("levelrewards");
		List<String> keysToRemove = new ArrayList<>();
		levelrewards.keySet().forEach(e -> {
			if (guild.getRoleById(levelrewards.getLong(e)) == null) {
				keysToRemove.add(e);
			}
		});
		for (int i = 0; i < keysToRemove.size(); i++) {
			levelrewards.remove(keysToRemove.get(i));
		}
	}
	public void modRoleCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray moderationroles = guildConfig.getJSONArray("moderationroles");
		for (int i = 0; i < moderationroles.length(); i++) {
			if (guild.getRoleById(moderationroles.getLong(i)) == null) {
				moderationroles.remove(i);
			}
		}
	}
	public void modInboxChannelCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		long modInbox = guildConfig.getLong("moderationinbox");
		if (guild.getTextChannelById(modInbox) == null) {
			guildConfig.put("moderationinbox", 0L);
		}
	}
	public void penaltiesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject penalties = guildConfig.getJSONObject("penalties");
		List<String> keysToRemove = new ArrayList<>();
		penalties.keySet().forEach(e -> {
			JSONArray penalty = penalties.getJSONArray(e);
			if (penalty.getString(0).equals("rr") && guild.getRoleById(penalty.getString(1)) == null) {
				keysToRemove.add(e);
			}
		});
		for (int i = 0; i < keysToRemove.size(); i++) {
			penalties.remove(keysToRemove.get(i));
		}
	}
	public void suggestionInboxCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		long suggestionInbox = guildConfig.getLong("suggestioninbox");
		if (guild.getTextChannelById(suggestionInbox) == null) {
			guildConfig.put("suggestioninbox", 0L);
		}
	}
	public void supportRoleCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray supportroles = guildConfig.getJSONArray("supportroles");
		for (int i = 0; i < supportroles.length(); i++) {
			if (guild.getRoleById(supportroles.getLong(i)) == null) {
				supportroles.remove(i);
			}
		}
	}
	public void supportTalkCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		long supporttalk = guildConfig.getLong("supporttalk");
		if (guild.getVoiceChannelById(supporttalk) == null) {
			guildConfig.put("supporttalk", 0L);
		}
	}
	public void welcomeMsgCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONArray welcomemsg = guildConfig.getJSONArray("welcomemsg");
		if (!welcomemsg.isEmpty()) {
			if (guild.getTextChannelById(welcomemsg.getLong(1)) == null) {
				guildConfig.put("welcomemsg", new JSONArray());
			}
		}
	}
	public void modMailsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject modmails = guildConfig.getJSONObject("modmails");
		List<String> keysToRemove = new ArrayList<>();
		modmails.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null) {
				keysToRemove.add(e);
			}
		});
		for (int i = 0; i < keysToRemove.size(); i++) {
			modmails.remove(keysToRemove.get(i));
		}
	}
	public void pollsCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject polls = guildConfig.getJSONObject("polls");
		List<String> keysToRemove1 = new ArrayList<>();
		polls.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null || polls.getJSONObject(e).isEmpty()) {
				keysToRemove1.add(e);
			} else {
				JSONObject channelConfig = polls.getJSONObject(e);
				List<String> keysToRemove2 = new ArrayList<>();
				channelConfig.keySet().forEach(o -> {
					if (guild.getTextChannelById(e).retrieveMessageById(o) == null || channelConfig.getJSONObject(o).isEmpty()) {
						keysToRemove2.add(o);
					}
				});
				for (int i = 0; i < keysToRemove2.size(); i++) {
					channelConfig.remove(keysToRemove2.get(i));
				}
				if (polls.getJSONObject(e).isEmpty()) {
					keysToRemove1.add(e);
				}
			}
		});
		for (int i = 0; i < keysToRemove1.size(); i++) {
			polls.remove(keysToRemove1.get(i));
		}
	}
	public void reactionRolesCheck(Guild guild) {
		JSONObject guildConfig = ConfigLoader.INSTANCE.getGuildConfig(guild);
		JSONObject reactionroles = guildConfig.getJSONObject("reactionroles");
		List<String> keysToRemove1 = new ArrayList<>();
		reactionroles.keySet().forEach(e -> {
			if (guild.getTextChannelById(e) == null || reactionroles.getJSONObject(e).isEmpty()) {
				keysToRemove1.add(e);
			} else {
				JSONObject channelConfig = reactionroles.getJSONObject(e);
				List<String> keysToRemove2 = new ArrayList<>();
				channelConfig.keySet().forEach(o -> {
					if (guild.getTextChannelById(e).retrieveMessageById(o) == null || channelConfig.getJSONObject(o).isEmpty()) {
						keysToRemove2.add(o);
					}
				});
				for (int i = 0; i < keysToRemove2.size(); i++) {
					channelConfig.remove(keysToRemove2.get(i));
				}
				if (reactionroles.getJSONObject(e).isEmpty()) {
					keysToRemove1.add(e);
				}
			}
		});
		for (int i = 0; i < keysToRemove1.size(); i++) {
			reactionroles.remove(keysToRemove1.get(i));
		}
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
		JSONObject userConfig = ConfigLoader.INSTANCE.getMemberConfig(guild, user);
		long customchannelcategory = userConfig.getLong("customchannelcategory");
		if (guild.getCategoryById(customchannelcategory) == null) {
			userConfig.put("customchannelcategory", 0L);
			this.customChannelCategoriesCheck(guild, true);
		}
	}
}
