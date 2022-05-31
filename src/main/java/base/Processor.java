package base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import commands.Command;
import commands.CommandList;
import commands.music.Stop;
import commands.utilities.Suggest;
import components.base.AnswerEngine;
import components.base.ConfigCheck;
import components.base.ConfigLoader;
import components.moderation.AutoModerator;
import components.moderation.ModMail;
import components.moderation.ServerUtilities;
import components.utilities.LevelEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
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
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Processor extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		final User user = event.getAuthor();
		if (user.isBot()) {
			return;
		}
		if(event.getChannelType().isGuild()) {
			final Guild guild = event.getGuild();
			//levelsystem
			LevelEngine.getInstance().messagereceived(event);
			//automoderation
			AutoModerator.getInstance().messagereceived(event);
			//suggestions
			long suggestchid = ConfigLoader.run.getGuildConfig(guild).getLong("suggestionchannel");
			if (suggestchid != 0 && event.getChannel().getIdLong() == suggestchid && !user.isBot()) {
				new Suggest().sendsuggestion(guild, event.getMember(), event.getMessage().getContentRaw());
				event.getMessage().delete().queue();
				return;
			}
			//support channel
			long supportchid = ConfigLoader.run.getGuildConfig(guild).getLong("supportchat");
			if (supportchid != 0 && event.getChannel().getIdLong() == supportchid && !user.isBot() && ConfigLoader.run.getGuildConfig(guild).getLong("supportrole") != 0) {
				if (guild.getCategoryById(ConfigLoader.run.getGuildConfig(guild).getLong("supportcategory")) == null) {
					Category cat = guild.createCategory("----------📝 Tickets ------------").complete();
					cat.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
					cat.upsertPermissionOverride(guild.getRoleById(ConfigLoader.run.getGuildConfig(guild).getLong("supportrole"))).setAllowed(Permission.VIEW_CHANNEL).queue();
					ConfigLoader.run.getGuildConfig(guild).put("supportcategory", cat.getIdLong());
				}
				int curcount = ConfigLoader.run.getGuildConfig(guild).getInt("ticketcount");
				int newcount = curcount + 1;
				TextChannel ntc = guild.createTextChannel(
						"Ticket #" + String.format("%05d", curcount),
						guild.getCategoryById(ConfigLoader.run.getGuildConfig(guild).getLong("supportcategory"))).complete();
				ntc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
				ntc.upsertPermissionOverride(guild.getRoleById(ConfigLoader.run.getGuildConfig(guild).getLong("supportrole"))).grant(Permission.VIEW_CHANNEL).queue();
				ntc.upsertPermissionOverride(event.getMember()).grant(Permission.VIEW_CHANNEL).queue();
				ntc.sendMessage(event.getMember().getAsMention() + ":\n" + event.getMessage().getContentDisplay() + "\n" 
						+ guild.getRoleById(ConfigLoader.run.getGuildConfig(guild).getLong("supportrole")).getAsMention()).queue();
				ConfigLoader.run.getGuildConfig(guild).put("ticketcount", newcount);
				event.getMessage().delete().queue();
				event.getTextChannel().getManager().setSlowmode(120).queue();
				return;
			}
			//modmail
			new ModMail(event, true);
		} else {
			new ModMail(event, false);
		}		
	}
	
	@Override
	public void onReady(ReadyEvent event) {
	    //delete Offline message
	    List<Guild> guilds = event.getJDA().getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			Guild guild = guilds.get(i);
			long msgid = ConfigLoader.run.getGuildConfig(guild).getLong("offlinemsg");
			if (msgid != 0) {
				guild.getTextChannelById(ConfigLoader.run.getGuildConfig(guild).getLong("levelmsgchannel")).retrieveMessageById(msgid).complete().delete().queue();
				ConfigLoader.run.getGuildConfig(guild).put("offlinemsg", Long.valueOf(0));
			}
		}
		//initialize Slashcommands
		CommandListUpdateAction clua = event.getJDA().updateCommands();
		CommandList utilitycmdList = new CommandList();
		List<String> utilitycmdnames = new ArrayList<>();
		utilitycmdnames.addAll(utilitycmdList.utilitycmds.keySet());
		for (int e = 0; e < utilitycmdnames.size(); e++) {
			Command cmd = utilitycmdList.utilitycmds.get(utilitycmdnames.get(e));
			clua.addCommands(cmd.initialize());
		}
		CommandList modcmdList = new CommandList();
		List<String> modcmdnames = new ArrayList<>();
		modcmdnames.addAll(modcmdList.moderationcmds.keySet());
		for (int e = 0; e < modcmdnames.size(); e++) {
			Command cmd = modcmdList.moderationcmds.get(modcmdnames.get(e));
			clua.addCommands(cmd.initialize().setDefaultEnabled(false));
		}
		CommandList musiccmdList = new CommandList();
		List<String> musiccmdnames = new ArrayList<>();
		musiccmdnames.addAll(musiccmdList.musiccmds.keySet());
		for (int e = 0; e < musiccmdnames.size(); e++) {
			Command cmd = musiccmdList.musiccmds.get(musiccmdnames.get(e));
			clua.addCommands(cmd.initialize());
		}
		clua.queue();
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		//perform Slash-Command
		CommandList commandList = new CommandList();
		Command utilitycmd;
		if ((utilitycmd = commandList.utilitycmds.get(event.getName())) != null) {
			utilitycmd.perform(event);
		}
		Command modcmd;
		if ((modcmd = commandList.moderationcmds.get(event.getName())) != null) {
			if (this.checkCategory(event.getTextChannel().getParentCategory(), guild) == null) {
				modcmd.perform(event);
			} else {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/base/processor:userchannel").convert()).queue();
			}
		}
		Command musiccmd;
		if ((musiccmd = commandList.musiccmds.get(event.getName())) != null) {
			musiccmd.perform(event);
		}
		//levelsystem
		LevelEngine.getInstance().slashcommand(event);
	}
	
	private User checkCategory(Category category, Guild guild) {
		return guild.getMemberById(ConfigLoader.run.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId())).getUser();
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		final Guild guild = event.getGuild();
		//assign Autoroles
		if (event.getMember().getUser().isBot()) {
			JSONArray botautoroles = ConfigLoader.run.getGuildConfig(guild).getJSONArray("botautoroles");
			if (!botautoroles.isEmpty()) {
				for (int i = 0; i < botautoroles.length(); i++) {
					Role role = guild.getRoleById(botautoroles.getLong(i));
					if (role == null) {
						ConfigLoader.run.removeValueFromArray(botautoroles, botautoroles.getLong(i));;
					} else {
						guild.addRoleToMember(event.getMember(), role).queue();
					}
				}
			}
		} else {
			JSONArray autoroles = ConfigLoader.run.getGuildConfig(guild).getJSONArray("autoroles");
			if (!autoroles.isEmpty()) {
				for (int i = 0; i < autoroles.length(); i++) {
					Role role = guild.getRoleById(autoroles.getLong(i));
					if (role == null) {
						ConfigLoader.run.removeValueFromArray(autoroles, autoroles.getLong(i));;
					} else {
						guild.addRoleToMember(event.getMember(), role).queue();
					}
				}
			}
			//send Welcomemessage
			String welcomemsgraw = ConfigLoader.run.getGuildConfig(guild).getString("welcomemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (!welcomemsgraw.equals("")) {
				String[] welcomemsg = welcomemsgraw.split(";");
				welcomemsg[0].replace("{server}", guild.getName());
				welcomemsg[0].replace("{member}", event.getMember().getAsMention());
				welcomemsg[0].replace("{membercount}", Integer.toString(guild.getMemberCount()));
				welcomemsg[0].replace("{date}", currentdate);
				guild.getTextChannelById(welcomemsg[1]).sendMessage(welcomemsg[0]).queue();
			}
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getUser().isBot()) {
			return;
		}
		//send goodbyemessage
		String goodbyemsgraw = ConfigLoader.run.getGuildConfig(guild).getString("goodbyemsg");
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
		String currentdate = date.format(formatter);
		if (!goodbyemsgraw.equals("")) {
			String[] goodbyemsg = goodbyemsgraw.split(";");
			goodbyemsg[0].replace("{server}", guild.getName());
			goodbyemsg[0].replace("{member}", user.getName());
			goodbyemsg[0].replace("{membercount}", Integer.toString(guild.getMemberCount()));
			goodbyemsg[0].replace("{date}", currentdate);
			goodbyemsg[0].replace("{timejoined}", event.getMember().getTimeJoined().format(formatter));
			event.getGuild().getTextChannelById(goodbyemsg[1]).sendMessage(goodbyemsg[0]).queue();
		}
		//check for users category
		if (ConfigLoader.run.getUserConfig(guild, user).getLong("cccategory") != 0) {
			Category ctg = guild.getCategoryById(ConfigLoader.run.getUserConfig(guild, user).getLong("cccategory"));
			List<GuildChannel> channels = ctg.getChannels();
			for (int i = 0; i < channels.size(); i++) {
				channels.get(i).delete().queue();
			}
			ctg.delete().queue();
		}
	}
	
	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		//check for Join2create-channel & create User-channel if true
		this.managej2cjoin(event.getGuild(), event.getMember(), event.getChannelJoined());
		//check if VoiceChannelLeft was a Userchannel
		this.managej2cleave(event.getGuild(), event.getMember().getUser(), event.getChannelLeft());
		//levelsystem
		LevelEngine.getInstance().voicemove(event);
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		//check for Join2create-channel & create User-channel if true
		this.managej2cjoin(event.getGuild(), event.getMember(), event.getChannelJoined());
		//levelsystem
		LevelEngine.getInstance().voicejoin(event);
	}
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		//check if VoiceChannelLeft was a Userchannel
		this.managej2cleave(event.getGuild(), event.getMember().getUser(), event.getChannelLeft());
	}
	
	private void managej2cjoin(Guild guild, Member member, AudioChannel audioChannel) {
		//check for Join2create-channel & create User-channel if true
		try {
			ConfigLoader.run.getGuildConfig(guild).getJSONObject("join2createchannels").get(audioChannel.getId());
		}catch (JSONException e) {return;}
		audioChannel.getPermissionContainer().upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VOICE_SPEAK).queue();
		Collection<Permission> perms = new LinkedList<Permission>();
		perms.add(Permission.VIEW_CHANNEL);
		perms.add(Permission.MANAGE_CHANNEL);
		perms.add(Permission.MANAGE_PERMISSIONS);
		perms.add(Permission.CREATE_INSTANT_INVITE);
		perms.add(Permission.VOICE_MUTE_OTHERS);
		perms.add(Permission.VOICE_SPEAK);
		ICategorizableChannel temp = (ICategorizableChannel) audioChannel;
		VoiceChannel nc = guild.createVoiceChannel(member.getEffectiveName() + "'s channel", temp.getParentCategory()).complete();
		nc.upsertPermissionOverride(member).setAllowed(perms).complete();
		guild.moveVoiceMember(member, nc).queue();
		ConfigLoader.run.getGuildConfig(guild).getJSONObject("createdchannels").put(nc.getId(), member.getUser().getIdLong());
	}
	
	private void managej2cleave(Guild guild, User user, AudioChannel audioChannel) {
		int conmemb = audioChannel.getMembers().size();
		//check if bot is the only one left in the channel (Then leave)
		if (conmemb == 1) {
			if (audioChannel.getMembers().get(0).equals(guild.getSelfMember())) {
				new Stop().stopandleave(guild);
				conmemb--;
			}
		}
		//check if VoiceChannelLeft was a Userchannel
		long ownerID = 0;
		JSONObject createdchannels = ConfigLoader.run.getGuildConfig(guild).getJSONObject("createdchannels");
		try {
			ownerID = createdchannels.getLong(audioChannel.getId());
		} catch (JSONException e) {return;}
		if (conmemb == 0) {
			ConfigLoader.run.getGuildConfig(guild).getJSONObject("createdchannels").remove(audioChannel.getId());
			audioChannel.delete().queue();
		} else {
			if (ownerID == user.getIdLong()) {
				Collection<Permission> perms = new LinkedList<Permission>();
				perms.add(Permission.VIEW_CHANNEL);
				perms.add(Permission.MANAGE_CHANNEL);
				perms.add(Permission.MANAGE_PERMISSIONS);
				perms.add(Permission.CREATE_INSTANT_INVITE);
				perms.add(Permission.VOICE_MUTE_OTHERS);
				perms.add(Permission.VOICE_SPEAK);
				Member newowner =  audioChannel.getMembers().get(0);
				createdchannels.put(audioChannel.getId(), newowner.getUser().getIdLong());
				audioChannel.getPermissionContainer().getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(guild.getMember(user)).setName(newowner.getEffectiveName() + "'s channel").queue();
			}
		}
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		final User user = event.getUser();
		final String channelID = event.getTextChannel().getId();
		final String msgID = event.getMessageId();
		if (user.isBot() || !event.isFromGuild()) {
			return;
		}
		final Guild guild = event.getGuild();
		//if reaction on poll, process reaction
		if (ConfigLoader.run.getPollConfig(guild, channelID, msgID) != null) {
			if (!event.getReactionEmote().getAsCodepoints().contains("U+20e3")) {
				event.getTextChannel().removeReactionById(msgID, event.getReactionEmote().getAsCodepoints(), user).queue();
			} else {
				this.addPollAnswer(channelID, msgID, event.getReactionEmote().getAsCodepoints(), guild, user);
				if (ConfigLoader.run.getPollConfig(guild, channelID, msgID).getBoolean("anonymous")) {
					event.getTextChannel().removeReactionById(msgID, event.getReactionEmote().getAsCodepoints(), user).queue();
				}
			}
			return;
		}
		//if reaction on reactionrole message, process reaction
		if (ConfigLoader.run.getReactionroleConfig(guild, channelID, msgID) != null) {
			JSONObject actions = ConfigLoader.run.getReactionroleConfig(guild, channelID, msgID);
			try {
				guild.addRoleToMember(user, guild.getRoleById(actions.getString(event.getReactionEmote().getAsCodepoints()))).queue();
			} catch (JSONException e) {}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		final User user = event.getUser();
		final String channelID = event.getTextChannel().getId();
		final String msgID = event.getMessageId();
		if(user.isBot() || !event.isFromGuild()) {
			return;
		}
		final Guild guild = event.getGuild();
		//if reaction on reactionrole message, process reaction
		if (ConfigLoader.run.getReactionroleConfig(guild, channelID, msgID) != null) {
			JSONObject actions = ConfigLoader.run.getReactionroleConfig(guild, channelID, msgID);
			try {
				guild.removeRoleFromMember(user, guild.getRoleById(actions.getLong(event.getReactionEmote().getAsCodepoints()))).queue();
			} catch (JSONException e) {}
		}
	}
	
	public void addPollAnswer(String channelID, String msgid, String emojiUnicode, Guild guild, User user) {
		JSONObject pollConfig = ConfigLoader.run.getPollConfig(guild, channelID, msgid);
		try {
			pollConfig.getJSONObject("answers").getInt(user.getId());
		} catch (JSONException e) {
			pollConfig.put("answercount", pollConfig.getInt("answercount") + 1);
		}
		String[] temp1 = emojiUnicode.split("U");
		int choice = Integer.parseInt(temp1[1])-31;
		pollConfig.getJSONObject("answers").put(user.getId(), choice);
	}
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		new ServerUtilities().rolecheck();
	}
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		new ServerUtilities().rolecheck();
	}
	
	@Override
	public void onChannelDelete(ChannelDeleteEvent event) {
		Guild guild = event.getGuild();
		new Thread(() -> {
			ConfigCheck.INSTANCE.checkGuildConfigs(guild);
		}).start();
		if (event.getChannelType().isAudio()) {
			String id = event.getChannel().getId();
			if (ConfigLoader.run.getGuildConfig(guild).getJSONObject("createdchannels").remove(id) != null) {
				return;
			}
		}
		if (event.isFromType(ChannelType.CATEGORY)) {
			Category ctg = (Category) event.getChannel();
			if (this.checkCategory(ctg, guild) != null) {
				ConfigLoader.run.getUserConfig(guild, this.checkCategory(ctg, guild)).put("customchannelcategory", Long.valueOf(0));
			}
			return;
		}
		if (event.getChannelType().isGuild()) {
			ICategorizableChannel channel = (ICategorizableChannel) event.getChannel();
			Category ctg = channel.getParentCategory();
			if (ctg != null) {
				if (ctg.getChannels().size() == 0) {
					if (this.checkCategory(ctg, guild) != null) {
						ConfigLoader.run.getUserConfig(guild, this.checkCategory(ctg, guild)).put("customchannelcategory", Long.valueOf(0));
						ctg.delete().queue();
					}
				}
			}
		}
	}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		new Thread(() -> {
			ConfigCheck.INSTANCE.checkGuildConfigs(event.getGuild());
		}).start();
	}
}