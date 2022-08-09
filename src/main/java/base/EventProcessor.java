package base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.engines.LanguageEngine;
import base.engines.Toolbox;
import base.engines.configs.ConfigLoader;
import base.engines.configs.ConfigVerifier;
import context_menu_commands.ContextMenuCommandList;
import context_menu_commands.assets.MessageContextEventHandler;
import context_menu_commands.assets.UserContextEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import slash_commands.CommandList;
import slash_commands.assets.SlashCommandEventHandler;
import slash_commands.engines.LevelEngine;
import slash_commands.engines.ServerUtilities;
import slash_commands.music.Stop;

public class EventProcessor extends ListenerAdapter {
	
	private CommandList commandList = new CommandList();
	private ContextMenuCommandList contextMenuCommandList = new ContextMenuCommandList();
	
	@Override
	public void onReady(ReadyEvent event) {
		CommandListUpdateAction clua = event.getJDA().updateCommands();
		commandList.slashCommandEventHandlers.forEach((name, cmd) -> {
			clua.addCommands(cmd.initialize());
		});
		contextMenuCommandList.messageContextEventHandlers.forEach((name, cmd) -> {
			clua.addCommands(cmd.initialize());
		});
		contextMenuCommandList.userContextEventHandlers.forEach((name, cmd) -> {
			clua.addCommands(cmd.initialize());
		});
		clua.queue();
	    List<Guild> guilds = event.getJDA().getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			Guild guild = guilds.get(i);
			try {
				long msgid = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("offlinemsg").getLong(0);
				if (msgid != 0L) {
					guild.getTextChannelById(ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("offlinemsg").getLong(1)).retrieveMessageById(msgid).complete().delete().queue();
				}
			} catch (JSONException | ErrorResponseException e) {}
			ConfigLoader.INSTANCE.getGuildConfig(guild).put("offlinemsg", new JSONArray());
		}
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		GUI.INSTANCE.increaseExecutionsCounter();
		SlashCommandEventHandler slashCommandEventHandler = null;
		if ((slashCommandEventHandler = commandList.slashCommandEventHandlers.get(event.getName().toLowerCase())) != null) {
			slashCommandEventHandler.execute(event);
		}
		LevelEngine.getInstance().slashcommand(event);
	}
	
	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		GUI.INSTANCE.increaseExecutionsCounter();
		UserContextEventHandler contextEventHandler = null;
		if ((contextEventHandler = contextMenuCommandList.userContextEventHandlers.get(event.getName().toLowerCase())) != null) {
			contextEventHandler.execute(event);
		}
	}
	
	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		if (event.isFromGuild()) {
			GUI.INSTANCE.increaseExecutionsCounter();
			MessageContextEventHandler contextEventHandler = null;
			if ((contextEventHandler = contextMenuCommandList.messageContextEventHandlers.get(event.getName().toLowerCase())) != null) {
				contextEventHandler.execute(event);
			}
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(null, null, this, "notsupported")).queue();	
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		//TODO Implement Modmail
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		GUI.INSTANCE.increaseMemberCounter();
		final Guild guild = event.getGuild();
		if (event.getMember().getUser().isBot()) {
			JSONArray botautoroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("botautoroles");
			if (!botautoroles.isEmpty()) {
				for (int i = 0; i < botautoroles.length(); i++) {
					Role role = guild.getRoleById(botautoroles.getLong(i));
					guild.addRoleToMember(event.getMember(), role).queue();
				}
			}
		} else {
			JSONArray autoroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("autoroles");
			if (!autoroles.isEmpty()) {
				for (int i = 0; i < autoroles.length(); i++) {
					Role role = guild.getRoleById(autoroles.getLong(i));
					guild.addRoleToMember(event.getMember(), role).queue();
				}
			}
			JSONArray welcomemsg = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("welcomemsg");
			if (!welcomemsg.isEmpty()) {
				if (welcomemsg.getBoolean(3)) {
					String title = Toolbox.processAutoMessage(welcomemsg.getString(1), guild, event.getUser(), false);
					String message = Toolbox.processAutoMessage(welcomemsg.getString(2), guild, event.getUser(), true);
					guild.getTextChannelById(welcomemsg.getLong(0)).sendMessageEmbeds(LanguageEngine.buildMessage(title, message, null)).queue();
				}
			}
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		GUI.INSTANCE.decreaseMemberCounter();
		if (event.getUser().isBot()) {
			return;
		}
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray goodbyemsg = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("goodbyemsg");
		if (!goodbyemsg.isEmpty()) {
			if (goodbyemsg.getBoolean(3)) {
				String title = Toolbox.processAutoMessage(goodbyemsg.getString(1), guild, event.getUser(), false);
				String message = Toolbox.processAutoMessage(goodbyemsg.getString(2), guild, event.getUser(), true);
				guild.getTextChannelById(goodbyemsg.getLong(0)).sendMessageEmbeds(LanguageEngine.buildMessage(title, message, null)).queue();
			}
		}
		if (ConfigLoader.INSTANCE.getMemberConfig(guild, user).getLong("customchannelcategory") != 0) {
			Category ctg = guild.getCategoryById(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getLong("customchannelcategory"));
			List<GuildChannel> channels = ctg.getChannels();
			for (int i = 0; i < channels.size(); i++) {
				channels.get(i).delete().queue();
			}
			ctg.delete().queue();
			ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("customchannelcategory", 0L);
		}
	}
	
	@Override
	public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getNewTimeBoosted() != null) {
			JSONArray boostmsg = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("boostmsg");
			if (!boostmsg.isEmpty()) {
				if (boostmsg.getBoolean(3)) {
					String title = Toolbox.processAutoMessage(boostmsg.getString(1), guild, user, false);
					String message = Toolbox.processAutoMessage(boostmsg.getString(2), guild, user, true);
					guild.getTextChannelById(boostmsg.getLong(0)).sendMessageEmbeds(LanguageEngine.buildMessage(title, message, null)).queue();
				}
			}
		}
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		GUI.INSTANCE.updateStatistics();
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		GUI.INSTANCE.updateStatistics();
	}
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		this.managej2cjoin(event.getGuild(), event.getMember(), event.getChannelJoined());
		this.managej2cleave(event.getGuild(), event.getMember().getUser(), event.getChannelLeft());
		
		LevelEngine.getInstance().voicemove(event);
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		this.managej2cjoin(event.getGuild(), event.getMember(), event.getChannelJoined());
		
		LevelEngine.getInstance().voicejoin(event);
	}
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		this.managej2cleave(event.getGuild(), event.getMember().getUser(), event.getChannelLeft());
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser().isBot() || !event.isFromGuild()) {
			return;
		}
		final User user = event.getUser();
		final String channelID = event.getChannel().getId();
		final String msgID = event.getMessageId();
		final Guild guild = event.getGuild();
		if (ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channelID, msgID) != null) {
			JSONObject actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channelID, msgID);
			try {
				guild.addRoleToMember(user, guild.getRoleById(actions.getString(event.getReaction().getEmoji().getAsReactionCode()))).queue();
			} catch (JSONException e) {}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		if (event.getUser().isBot() || !event.isFromGuild()) {
			return;
		}
		final User user = event.getUser();
		final String channelID = event.getChannel().getId();
		final String msgID = event.getMessageId();
		final Guild guild = event.getGuild();
		if (ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channelID, msgID) != null) {
			JSONObject actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channelID, msgID);
			try {
				guild.removeRoleFromMember(user, guild.getRoleById(actions.getLong(event.getReaction().getEmoji().getAsReactionCode()))).queue();
			} catch (JSONException e) {}
		}
	}
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		ServerUtilities.rolecheck();
	}
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		ServerUtilities.rolecheck();
	}
	
	@Override
	public void onChannelCreate(ChannelCreateEvent event) {}
	
	@Override
	public void onChannelDelete(ChannelDeleteEvent event) {
		Guild guild = event.getGuild();
		ConfigVerifier.RUN.guildCheck(guild);
		if (event.isFromType(ChannelType.CATEGORY)) {
			Category ctg = (Category) event.getChannel();
			if (Toolbox.checkCategory(ctg, guild) != null) {
				ConfigVerifier.RUN.userCheck(guild, Toolbox.checkCategory(ctg, guild));
			}
		}
	}
	
	@Override
	public void onRoleCreate(RoleCreateEvent event) {}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		new Thread(() -> {
			ConfigVerifier.RUN.guildCheck(event.getGuild());
		}).start();
	}
	
	//Tool methods
	private void managej2cjoin(Guild guild, Member member, AudioChannel audioChannel) {
		JSONObject channelConfig = null;
		try {
			channelConfig = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(audioChannel.getId());
		} catch (JSONException e) {return;}
		if (channelConfig != null) {
			audioChannel.getPermissionContainer().upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VOICE_SPEAK).queue();
			Collection<Permission> defperms = new LinkedList<Permission>();
			defperms.add(Permission.VIEW_CHANNEL);
			defperms.add(Permission.VOICE_SPEAK);
			Collection<Permission> perms = new LinkedList<Permission>();
			if (channelConfig.getBoolean("configurable")) {
				perms.add(Permission.MANAGE_CHANNEL);
				perms.add(Permission.MANAGE_PERMISSIONS);
				perms.add(Permission.CREATE_INSTANT_INVITE);
				perms.add(Permission.VOICE_MUTE_OTHERS);
			}
			Category ctg = null;
			try {
				ICategorizableChannel temp = (ICategorizableChannel) audioChannel;
				ctg = temp.getParentCategory();
			} catch (ClassCastException ex) {};
			JSONObject createdChannels = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("createdchannels");
			try {
				createdChannels.getJSONObject(audioChannel.getId());
			} catch (JSONException ex) {
				createdChannels.put(audioChannel.getId(), new JSONObject());
			}
			int index = createdChannels.getJSONObject(audioChannel.getId()).keySet().size() + 1;
			String name = channelConfig.getString("name")
			    .replace("{member}", member.getEffectiveName())
			    .replace("{number}", String.valueOf(index));
			VoiceChannel nc = guild.createVoiceChannel(name, ctg).complete();
			nc.upsertPermissionOverride(guild.getPublicRole()).setAllowed(defperms).complete();
			nc.upsertPermissionOverride(member).setAllowed(perms).complete();
			if (channelConfig.getInt("limit") > 0) {
				nc.getManager().setUserLimit(channelConfig.getInt("limit")).queue();
			}
			guild.moveVoiceMember(member, nc).queue();
			createdChannels.getJSONObject(audioChannel.getId()).put(nc.getId(), new JSONArray().put(0, member.getUser().getIdLong()).put(1, index));
		}
	}
	
	private void managej2cleave(Guild guild, User user, AudioChannel audioChannel) {
		int conmemb = audioChannel.getMembers().size();
		if (conmemb == 1) {
			if (audioChannel.getMembers().get(0).equals(guild.getSelfMember())) {
				Stop.stopandleave(guild);
				conmemb--;
			}
		}
		JSONObject createdchannels = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("createdchannels");
		List<String> parentChannels = new ArrayList<>();
		parentChannels.addAll(createdchannels.keySet());
		for (int i = 0; i < parentChannels.size(); i++) {
			try {
				JSONObject parentChannelData = createdchannels.getJSONObject(parentChannels.get(i));
				JSONArray channelData = parentChannelData.getJSONArray(audioChannel.getId());
				JSONObject parentChannelConfig = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(parentChannels.get(i));
				long ownerID = channelData.getLong(0);
				if (conmemb == 0) {
					audioChannel.delete().queue();
					parentChannelData.remove(audioChannel.getId());
					int index = channelData.getInt(1);
					//Update index numbers
					List<String> subChannels = new ArrayList<>();
					subChannels.addAll(parentChannelData.keySet());
					for (int a = 0; a < subChannels.size(); a++) {
						JSONArray subChannelData = parentChannelData.getJSONArray(subChannels.get(a));
						VoiceChannel target = guild.getVoiceChannelById(subChannels.get(a));
						int currentIndex = subChannelData.getInt(1);
						if (subChannelData.getInt(1) > index
								&& parentChannelConfig.getString("name").contains("{number}")
								&& target.getName().contains(String.valueOf(currentIndex))) {
							target.getManager().setName(target.getName().replaceFirst(String.valueOf(currentIndex), String.valueOf(currentIndex - 1))).queue();
							subChannelData.put(1, currentIndex + 1);
						}
					}				
				} else {
					if (ownerID == user.getIdLong()) {
						Collection<Permission> perms = new LinkedList<Permission>();
						if (parentChannelConfig.getBoolean("configurable")) {
							perms.add(Permission.MANAGE_CHANNEL);
							perms.add(Permission.MANAGE_PERMISSIONS);
							perms.add(Permission.CREATE_INSTANT_INVITE);
							perms.add(Permission.VOICE_MUTE_OTHERS);
						}
						Member newowner =  audioChannel.getMembers().get(0);
						String name = audioChannel.getName().replace(guild.getMember(user).getEffectiveName(), newowner.getEffectiveName());
						audioChannel.getManager().setName(name).queue();
						audioChannel.getPermissionContainer().upsertPermissionOverride(newowner).setAllowed(perms).queue();
						audioChannel.getPermissionContainer().getPermissionOverride(guild.getMember(user)).delete().queue();
						channelData.put(0, newowner.getIdLong());
						audioChannel.getPermissionContainer().getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(guild.getMember(user)).setName(name).queue();
					}
				}
				i = parentChannels.size();
			} catch (JSONException ex) {}
		}
	}
}