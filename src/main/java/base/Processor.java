package base;

import java.time.OffsetDateTime;
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
import components.base.ConfigLoader;
import components.base.ConfigVerifier;
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
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
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
			long msgid = ConfigLoader.getGuildConfig(guild).getLong("offlinemsg");
			if (msgid != 0) {
				guild.getTextChannelById(ConfigLoader.getGuildConfig(guild).getLong("systeminfochannel")).retrieveMessageById(msgid).complete().delete().queue();
				ConfigLoader.getGuildConfig(guild).put("offlinemsg", 0L);
			}
		}
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
		if (event.getUser().isBot()) {
			return;
		}
		final Guild guild = event.getGuild();
		final User user = event.getUser();
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
				event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/base/processor:userchannel").convert()).queue();
			}
		}
		Command musiccmd;
		if ((musiccmd = commandList.musiccmds.get(event.getName())) != null) {
			musiccmd.perform(event);
		}
		LevelEngine.getInstance().slashcommand(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		final User user = event.getAuthor();
		if (event.getChannelType().isGuild()) {
			final Guild guild = event.getGuild();
			long suggestchid = ConfigLoader.getGuildConfig(guild).getLong("suggestionchannel");
			if (suggestchid != 0 && event.getChannel().getIdLong() == suggestchid && !user.isBot()) {
				new Suggest().sendsuggestion(guild, event.getMember(), event.getMessage().getContentRaw());
				event.getMessage().delete().queue();
				return;
			}
			long supportchid = ConfigLoader.getGuildConfig(guild).getLong("supportchat");
			if (supportchid != 0 && event.getChannel().getIdLong() == supportchid && !user.isBot() && ConfigLoader.getGuildConfig(guild).getLong("supportrole") != 0) {
				if (ConfigLoader.getGuildConfig(guild).getLong("supportcategory") == 0) {
					Category cat = guild.createCategory("----------📝 Tickets ------------").complete();
					cat.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
					cat.upsertPermissionOverride(guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole"))).setAllowed(Permission.VIEW_CHANNEL).queue();
					ConfigLoader.getGuildConfig(guild).put("supportcategory", cat.getIdLong());
				}
				int curcount = ConfigLoader.getGuildConfig(guild).getInt("ticketcount");
				int newcount = curcount + 1;
				TextChannel ntc = guild.createTextChannel(
						"Ticket #" + String.format("%05d", curcount),
						guild.getCategoryById(ConfigLoader.getGuildConfig(guild).getLong("supportcategory"))).complete();
				ntc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
				ntc.upsertPermissionOverride(guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole"))).grant(Permission.VIEW_CHANNEL).queue();
				ntc.upsertPermissionOverride(event.getMember()).grant(Permission.VIEW_CHANNEL).queue();
				ntc.sendMessage(event.getMember().getAsMention() + ":\n" + event.getMessage().getContentDisplay() + "\n" 
						+ guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole")).getAsMention()).queue();
				ConfigLoader.getGuildConfig(guild).put("ticketcount", newcount);
				ConfigLoader.getGuildConfig(guild).getJSONArray("ticketchannels").put(ntc.getIdLong());
				event.getMessage().delete().queue();
				event.getTextChannel().getManager().setSlowmode(120).queue();
				return;
			}
			new ModMail(event, true);
			LevelEngine.getInstance().messagereceived(event);
		} else {
			new ModMail(event, false);
		}		
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
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
			String welcomemsgraw = ConfigLoader.getGuildConfig(guild).getString("welcomemsg");
			if (!welcomemsgraw.equals("")) {
				String[] welcomemsg = welcomemsgraw.split(";");
				welcomemsg[0].replace("{server}", guild.getName());
				welcomemsg[0].replace("{member}", event.getMember().getAsMention());
				welcomemsg[0].replace("{membercount}", Integer.toString(guild.getMemberCount()));
				welcomemsg[0].replace("{date}", OffsetDateTime.now().format(AnswerEngine.formatter));
				guild.getTextChannelById(welcomemsg[1]).sendMessage(welcomemsg[0]).queue();
			}
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		Guild guild = event.getGuild();
		User user = event.getUser();
		String goodbyemsgraw = ConfigLoader.getGuildConfig(guild).getString("goodbyemsg");
		if (!goodbyemsgraw.equals("")) {
			String[] goodbyemsg = goodbyemsgraw.split(";");
			goodbyemsg[0].replace("{server}", guild.getName());
			goodbyemsg[0].replace("{member}", user.getName());
			goodbyemsg[0].replace("{membercount}", Integer.toString(guild.getMemberCount()));
			goodbyemsg[0].replace("{date}", OffsetDateTime.now().format(AnswerEngine.formatter));
			goodbyemsg[0].replace("{timejoined}", event.getMember().getTimeJoined().format(AnswerEngine.formatter));
			event.getGuild().getTextChannelById(goodbyemsg[1]).sendMessage(goodbyemsg[0]).queue();
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
			if (this.checkCategory(ctg, guild) != null) {
				ConfigVerifier.run.userCheck(guild, this.checkCategory(ctg, guild));
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
			ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels").get(audioChannel.getId());
		} catch (JSONException e) {return;}
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
		ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels").put(nc.getId(), member.getUser().getIdLong());
	}
	
	private void managej2cleave(Guild guild, User user, AudioChannel audioChannel) {
		int conmemb = audioChannel.getMembers().size();
		if (conmemb == 1) {
			if (audioChannel.getMembers().get(0).equals(guild.getSelfMember())) {
				new Stop().stopandleave(guild);
				conmemb--;
			}
		}
		long ownerID = 0;
		JSONObject createdchannels = ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels");
		try {
			ownerID = createdchannels.getLong(audioChannel.getId());
		} catch (JSONException e) {return;}
		if (conmemb == 0) {
			ConfigLoader.getGuildConfig(guild).getJSONObject("createdchannels").remove(audioChannel.getId());
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
	
	private User checkCategory(Category category, Guild guild) {
		try {
			return Bot.run.jda.getUserById(ConfigLoader.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId()));
		} catch (JSONException e) {
			return null;
		}
	}
}