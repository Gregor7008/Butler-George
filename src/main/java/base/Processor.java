package base;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import commands.CommandList;
import commands.music.Stop;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.moderation.ModMail;
import components.commands.utilities.LevelEngine;
import components.utilities.ConfigVerifier;
import components.utilities.ServerUtilities;
import components.utilities.Toolbox;
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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Processor extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent event) {
	    List<Guild> guilds = event.getJDA().getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			Guild guild = guilds.get(i);
			try {
				long msgid = ConfigLoader.getGuildConfig(guild).getJSONArray("offlinemsg").getLong(0);
				if (msgid != 0) {
					guild.getTextChannelById(ConfigLoader.getGuildConfig(guild).getJSONArray("offlinemsg").getLong(1)).retrieveMessageById(msgid).complete().delete().queue();
					ConfigLoader.getGuildConfig(guild).put("offlinemsg", new JSONArray());
				}
			} catch (JSONException e) {}
		}
		CommandListUpdateAction clua = event.getJDA().updateCommands();
		new CommandList().commands.forEach((name, cmd) -> {
			clua.addCommands(cmd.initialize());
		});
		clua.queue();
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		GUI.get.increaseExecutionsCounter();
		CommandList commandList = new CommandList();
		Command command = null;
		if ((command = commandList.commands.get(event.getName())) != null) {
			if (command.canBeAccessedBy(event.getMember()) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
				command.perform(event);
			}
		}
		LevelEngine.getInstance().slashcommand(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		//final User user = event.getAuthor();
		if (event.getChannelType().isGuild()) {
			//final Guild guild = event.getGuild();
//			long supportchid = ConfigLoader.getGuildConfig(guild).getLong("supportchat");
//			if (supportchid != 0 && event.getChannel().getIdLong() == supportchid && !user.isBot() && ConfigLoader.getGuildConfig(guild).getLong("supportrole") != 0) {
//				if (ConfigLoader.getGuildConfig(guild).getLong("supportcategory") == 0) {
//					Category cat = guild.createCategory("----------📝 Tickets ------------").complete();
//					cat.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
//					cat.upsertPermissionOverride(guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole"))).setAllowed(Permission.VIEW_CHANNEL).queue();
//					ConfigLoader.getGuildConfig(guild).put("supportcategory", cat.getIdLong());
//				}
//				int curcount = ConfigLoader.getGuildConfig(guild).getInt("ticketcount");
//				int newcount = curcount + 1;
//				TextChannel ntc = guild.createTextChannel(
//						"Ticket #" + String.format("%05d", curcount),
//						guild.getCategoryById(ConfigLoader.getGuildConfig(guild).getLong("supportcategory"))).complete();
//				ntc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
//				ntc.upsertPermissionOverride(guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole"))).grant(Permission.VIEW_CHANNEL).queue();
//				ntc.upsertPermissionOverride(event.getMember()).grant(Permission.VIEW_CHANNEL).queue();
//				ntc.sendMessage(event.getMember().getAsMention() + ":\n" + event.getMessage().getContentDisplay() + "\n" 
//						+ guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole")).getAsMention()).queue();
//				ConfigLoader.getGuildConfig(guild).put("ticketcount", newcount);
//				ConfigLoader.getGuildConfig(guild).getJSONArray("ticketchannels").put(ntc.getIdLong());
//				event.getMessage().delete().queue();
//				event.getTextChannel().getManager().setSlowmode(120).queue();
//				return;
//			}
			new ModMail(event, true);
			LevelEngine.getInstance().messagereceived(event);
		} else {
			new ModMail(event, false);
		}		
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		GUI.get.updateStatistics();
		final Guild guild = event.getGuild();
		if (event.getMember().getUser().isBot()) {
			JSONArray botautoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles");
			if (!botautoroles.isEmpty()) {
				for (int i = 0; i < botautoroles.length(); i++) {
					Role role = guild.getRoleById(botautoroles.getLong(i));
					guild.addRoleToMember(event.getMember(), role).queue();
				}
			}
		} else {
			JSONArray autoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("autoroles");
			if (!autoroles.isEmpty()) {
				for (int i = 0; i < autoroles.length(); i++) {
					Role role = guild.getRoleById(autoroles.getLong(i));
					guild.addRoleToMember(event.getMember(), role).queue();
				}
			}
			JSONArray welcomemsg = ConfigLoader.getGuildConfig(guild).getJSONArray("welcomemsg");
			if (!welcomemsg.isEmpty()) {
				if (welcomemsg.getBoolean(3)) {
					String msg = welcomemsg.getString(1);
					msg.replace("{server}", guild.getName());
					msg.replace("{user}", event.getMember().getAsMention());
					msg.replace("{membercount}", Integer.toString(guild.getMemberCount()));
					msg.replace("{date}", OffsetDateTime.now().format(LanguageEngine.formatter));
					guild.getTextChannelById(welcomemsg.getLong(1)).sendMessage(msg).queue();
				}
			}
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		GUI.get.updateStatistics();
		if (event.getUser().isBot()) {
			return;
		}
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray goodbyemsg = ConfigLoader.getGuildConfig(guild).getJSONArray("goodbyemsg");
		if (!goodbyemsg.isEmpty()) {
			if (goodbyemsg.getBoolean(3)) {
				String msg = goodbyemsg.getString(1);
				msg.replace("{server}", guild.getName());
				msg.replace("{member}", event.getMember().getAsMention());
				msg.replace("{membercount}", Integer.toString(guild.getMemberCount()));
				msg.replace("{date}", OffsetDateTime.now().format(LanguageEngine.formatter));
				guild.getTextChannelById(goodbyemsg.getLong(1)).sendMessage(msg).queue();
			}
		}
		if (ConfigLoader.getMemberConfig(guild, user).getLong("customchannelcategory") != 0) {
			Category ctg = guild.getCategoryById(ConfigLoader.getMemberConfig(guild, user).getLong("customchannelcategory"));
			List<GuildChannel> channels = ctg.getChannels();
			for (int i = 0; i < channels.size(); i++) {
				channels.get(i).delete().queue();
			}
			ctg.delete().queue();
			ConfigLoader.getMemberConfig(guild, user).put("customchannelcategory", 0L);
		}
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		GUI.get.updateStatistics();
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		GUI.get.updateStatistics();
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
		final String channelID = event.getTextChannel().getId();
		final String msgID = event.getMessageId();
		final Guild guild = event.getGuild();
		if (ConfigLoader.getReactionMessageConfig(guild, channelID, msgID) != null) {
			JSONObject actions = ConfigLoader.getReactionMessageConfig(guild, channelID, msgID);
			try {
				guild.addRoleToMember(user, guild.getRoleById(actions.getString(event.getReactionEmote().getAsCodepoints()))).queue();
			} catch (JSONException e) {}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		if (event.getUser().isBot() || !event.isFromGuild()) {
			return;
		}
		final User user = event.getUser();
		final String channelID = event.getTextChannel().getId();
		final String msgID = event.getMessageId();
		final Guild guild = event.getGuild();
		if (ConfigLoader.getReactionMessageConfig(guild, channelID, msgID) != null) {
			JSONObject actions = ConfigLoader.getReactionMessageConfig(guild, channelID, msgID);
			try {
				guild.removeRoleFromMember(user, guild.getRoleById(actions.getLong(event.getReactionEmote().getAsCodepoints()))).queue();
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
		ConfigVerifier.run.guildCheck(guild);
		if (event.isFromType(ChannelType.CATEGORY)) {
			Category ctg = (Category) event.getChannel();
			if (Toolbox.checkCategory(ctg, guild) != null) {
				ConfigVerifier.run.userCheck(guild, Toolbox.checkCategory(ctg, guild));
			}
		}
	}
	
	@Override
	public void onRoleCreate(RoleCreateEvent event) {}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		new Thread(() -> {
			ConfigVerifier.run.guildCheck(event.getGuild());
		}).start();
	}
	
	//Tool methods
	private void managej2cjoin(Guild guild, Member member, AudioChannel audioChannel) {
		try {
			JSONObject channelConfig = ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(audioChannel.getId());
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
			String name = channelConfig.getString("name");
			name.replace("{member}", member.getEffectiveName());
			//name.replace("{number}", String.valueOf(ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels").getJSONObject(audioChannel.getId()).keySet().size() + 1));
			VoiceChannel nc = guild.createVoiceChannel(name, ctg).complete();
			nc.upsertPermissionOverride(guild.getPublicRole()).setAllowed(defperms).complete();
			nc.upsertPermissionOverride(member).setAllowed(perms).complete();
			if (channelConfig.getInt("limit") > 0) {
				nc.getManager().setUserLimit(channelConfig.getInt("limit"));
			}
			guild.moveVoiceMember(member, nc).queue();
			ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels").getJSONObject(audioChannel.getId()).put(nc.getId(), member.getUser().getIdLong());
		} catch (JSONException e) {return;}
	}
	
	private void managej2cleave(Guild guild, User user, AudioChannel audioChannel) {
		int conmemb = audioChannel.getMembers().size();
		if (conmemb == 1) {
			if (audioChannel.getMembers().get(0).equals(guild.getSelfMember())) {
				new Stop().stopandleave(guild);
				conmemb--;
			}
		}
		JSONObject createdchannels = ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels");
		createdchannels.keySet().forEach(e -> {
			try {
				String parentID = e;
				long ownerID = createdchannels.getJSONObject(e).getLong(audioChannel.getId());
				if (audioChannel.getMembers().size() == 0) {
					ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels").getJSONObject(e).remove(audioChannel.getId());
					audioChannel.delete().queue();
					createdchannels.getJSONObject(parentID).remove(audioChannel.getId());
				} else {
					if (ownerID == user.getIdLong()) {
						JSONObject channelConfig = ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(parentID);
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
						Member newowner =  audioChannel.getMembers().get(0);
						String name = channelConfig.getString("name");
						name.replace("{member}", newowner.getEffectiveName());
						//name.replace("{number}", String.valueOf(ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels").getJSONObject(audioChannel.getId()).keySet().size() + 1));
						VoiceChannel nc = guild.createVoiceChannel(name, ctg).complete();
						nc.upsertPermissionOverride(guild.getPublicRole()).setAllowed(defperms).complete();
						nc.upsertPermissionOverride(newowner).setAllowed(perms).complete();
						createdchannels.getJSONObject(parentID).put(audioChannel.getId(), newowner.getUser().getIdLong());
						audioChannel.getPermissionContainer().getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(guild.getMember(user)).setName(name).queue();
					}
				}
			} catch (JSONException ex) {}
		});
	}
}