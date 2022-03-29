package base;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import commands.Command;
import commands.CommandList;
import commands.music.Stop;
import commands.utilities.Suggest;
import components.base.AnswerEngine;
import components.base.Configcheck;
import components.base.Configloader;
import components.moderation.AutoModerator;
import components.moderation.ModMail;
import components.moderation.ServerUtilities;
import components.utilities.LevelEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
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
			//ModMail
			if (Configloader.INSTANCE.getMailConfig1(event.getChannel().getId()) != null) {
				if (user.isBot()) {
					return;
				}
				PrivateChannel pc = Bot.INSTANCE.jda.openPrivateChannelById(Configloader.INSTANCE.getMailConfig1(event.getChannel().getId())).complete();
				pc.sendMessage(event.getMessage().getContentDisplay()).queue();
				return;
			}
			//Suggestions
			String suggestid = Configloader.INSTANCE.getGuildConfig(guild, "suggest");
			if (!suggestid.equals("") && event.getChannel().getId().equals(suggestid) && !user.isBot()) {
				new Suggest().sendsuggestion(guild, event.getMember(), event.getMessage().getContentRaw());
				event.getMessage().delete().queue();
				return;
			}
			//Support channel
			String supportid = Configloader.INSTANCE.getGuildConfig(guild, "supportchat");
			if (!supportid.equals("") && event.getChannel().getId().equals(supportid) && !user.isBot() && !Configloader.INSTANCE.getGuildConfig(guild, "supportrole").equals("")) {
				if (guild.getCategoryById(Configloader.INSTANCE.getGuildConfig(guild, "supportcategory")) == null) {
					Category cat = guild.createCategory("Supportchat").complete();
					cat.createPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
					cat.createPermissionOverride(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole"))).setAllow(Permission.VIEW_CHANNEL).queue();
					Configloader.INSTANCE.setGuildConfig(guild, "supportcategory", cat.getId());
				}
				String curcount = Configloader.INSTANCE.getGuildConfig(guild, "ticketcount");
				int newcount = Integer.parseInt(curcount) + 1;
				TextChannel ntc = guild.createTextChannel(
						"Ticket #" + curcount,
						guild.getCategoryById(Configloader.INSTANCE.getGuildConfig(guild, "supportcategory"))).complete();
				ntc.putPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
				ntc.putPermissionOverride(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole"))).setAllow(Permission.VIEW_CHANNEL).queue();
				ntc.putPermissionOverride(event.getMember()).setAllow(Permission.VIEW_CHANNEL).queue();
				ntc.sendMessage(event.getMember().getAsMention() + ":\n" + event.getMessage().getContentDisplay() + "\n" + guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole")).getAsMention()).queue();
				Configloader.INSTANCE.setGuildConfig(guild, "ticketcount", String.format("%05d", newcount));
				event.getMessage().delete().queue();
				event.getTextChannel().getManager().setSlowmode(120).queue();
				return;
			}
		} else {
			//process modmail
			new ModMail(event);
		}
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		//setup Bot
		new Configloader();
	    new Configcheck();
	    //delete Offline message
	    List<Guild> guilds = event.getJDA().getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			Guild guild = guilds.get(i);
			String idpack = Configloader.INSTANCE.getGuildConfig(guild, "offlinemsg");
			if (!idpack.equals("")) {
				String[] ids = idpack.split("_");
				guild.getTextChannelById(ids[0]).retrieveMessageById(ids[1]).complete().delete().queue();
				Configloader.INSTANCE.setGuildConfig(guild, "offlinemsg", "");
			}
		}
		//initialize Slashcommands
		CommandListUpdateAction clua = Bot.INSTANCE.jda.updateCommands();
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
			clua.addCommands(cmd.initialize());
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
			if (Configloader.INSTANCE.getGuildConfig(guild, "ignored").contains(event.getChannel().getId()) && !event.getName().equals("embed") && !event.getName().equals("poll")) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/base/processor:ignoredchannel")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
				return;
			}
			utilitycmd.perform(event);
		}
		Command modcmd;
		if ((modcmd = commandList.moderationcmds.get(event.getName())) != null) {
			if (!this.checkCategory(event.getTextChannel().getParentCategory(), guild)) {
				modcmd.perform(event);
			} else {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/base/processor:userchannel")).queue();
				return;
			}
		}
		Command musiccmd;
		if ((musiccmd = commandList.musiccmds.get(event.getName())) != null) {
			if (Configloader.INSTANCE.getGuildConfig(guild, "ignored").contains(event.getChannel().getId())) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/base/processor:ignoredchannel")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
				return;
			}
			musiccmd.perform(event);
		}
		//levelsystem
		LevelEngine.getInstance().slashcommand(event);
	}
	
	private boolean checkCategory(Category category, Guild guild) {
		File guilddir = new File(Bot.environment + "/configs/user/" + guild.getId());
		File[] filelist = guilddir.listFiles();
		if (filelist == null) {
			return false;
		}
		for (int i = 0; i < filelist.length; i++) {
			String[] temp1 = filelist[i].getName().split(".properties");
			User cuser = Bot.INSTANCE.jda.retrieveUserById(temp1[0]).complete();
			String ccid = Configloader.INSTANCE.getUserConfig(guild, cuser, "cccategory");
			if (!ccid.equals("")) {
				if (category.equals(guild.getCategoryById(ccid))) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		final Guild guild = event.getGuild();
		//assign Autoroles
		if (event.getMember().getUser().isBot()) {
			String botautorolesraw = Configloader.INSTANCE.getGuildConfig(guild, "botautoroles");
			if (!botautorolesraw.equals("")) {
				String[] botautoroles = botautorolesraw.split(";");
				for (int i = 0; i < botautoroles.length; i++) {
					Role role = guild.getRoleById(botautoroles[i]);
					if (role == null) {
						Configloader.INSTANCE.deleteGuildConfig(guild, "botautoroles", botautoroles[i]);
					} else {
						guild.addRoleToMember(event.getMember(), role).queue();
					}
				}
			}
		} else {
			String autorolesraw = Configloader.INSTANCE.getGuildConfig(guild, "autoroles");
			if (!autorolesraw.equals("")) {
				String[] autoroles = autorolesraw.split(";");
				for (int i = 0; i < autoroles.length; i++) {
					Role role = guild.getRoleById(autoroles[i]);
					if (role == null) {
						Configloader.INSTANCE.deleteGuildConfig(guild, "autoroles", autoroles[i]);
					} else {
						guild.addRoleToMember(event.getMember(), role).queue();
					}
				}
			}
			//send Welcomemessage
			String welcomemsgraw = Configloader.INSTANCE.getGuildConfig(guild, "welcomemsg");
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
		if (event.getUser().isBot()) {
			return;
		}
		//send goodbyemessage
		String goodbyemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
		String currentdate = date.format(formatter);
		if (!goodbyemsgraw.equals("")) {
			String[] goodbyemsg = goodbyemsgraw.split(";");
			goodbyemsg[0].replace("{server}", event.getGuild().getName());
			goodbyemsg[0].replace("{member}", event.getMember().getEffectiveName());
			goodbyemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
			goodbyemsg[0].replace("{date}", currentdate);
			goodbyemsg[0].replace("{timejoined}", event.getMember().getTimeJoined().format(formatter));
			event.getGuild().getTextChannelById(goodbyemsg[1]).sendMessage(goodbyemsg[0]).queue();
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
		String j2cids = Configloader.INSTANCE.getGuildConfig(guild, "join2create");
		if (j2cids.contains(audioChannel.getId())) {
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
			nc.putPermissionOverride(member).setAllow(perms).complete();
			guild.moveVoiceMember(member, nc).queue();
			Configloader.INSTANCE.addGuildConfig(guild, "j2cs", nc.getId() + "-" + member.getUser().getId());
		}
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
		if (Configloader.INSTANCE.getGuildConfig(guild, "j2cs").contains(audioChannel.getId())) {
			if (conmemb == 0) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "j2cs", audioChannel.getId() + "-" + user.getId());
				audioChannel.delete().queue();
			} else {
				if (Configloader.INSTANCE.getGuildConfig(guild, "j2cs").contains(audioChannel.getId() + "-" + user.getId())) {
					Collection<Permission> perms = new LinkedList<Permission>();
					perms.add(Permission.VIEW_CHANNEL);
					perms.add(Permission.MANAGE_CHANNEL);
					perms.add(Permission.MANAGE_PERMISSIONS);
					perms.add(Permission.CREATE_INSTANT_INVITE);
					perms.add(Permission.VOICE_MUTE_OTHERS);
					perms.add(Permission.VOICE_SPEAK);
					Configloader.INSTANCE.deleteGuildConfig(guild, "j2cs", audioChannel.getId() + "-" + user.getId());
					Member newowner =  audioChannel.getMembers().get(0);
					Configloader.INSTANCE.addGuildConfig(guild, "j2cs", audioChannel.getId() + "-" + newowner.getUser().getId());
					audioChannel.getPermissionContainer().getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(guild.getMember(user)).setName(newowner.getEffectiveName() + "'s channel").queue();
				}
			}
		}
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		final User user = event.getUser();
		if (user.isBot()) {
			return;
		}
		if (event.getChannelType().isGuild()) {
			final Guild guild = event.getGuild();
			//if reaction on poll, process reaction
			if (Configloader.INSTANCE.findPollConfig(guild, event.getMessageId()) != null) {
				if (!event.getReactionEmote().getAsCodepoints().contains("U+20e3")) {
					event.getTextChannel().removeReactionById(event.getMessageId(), event.getReactionEmote().getAsCodepoints(), user).queue();
				} else {
					this.addPollAnswer(event.getMessageId(), event.getReactionEmote().getAsCodepoints(), guild, user);
					if (Boolean.parseBoolean(Configloader.INSTANCE.getPollConfig(guild, event.getMessageId(), "anonymous"))) {
						event.getTextChannel().removeReactionById(event.getMessageId(), event.getReactionEmote().getAsCodepoints(), user).queue();
					}
				}
				return;
			}
			//if reaction on reactionrole message, process reaction
			if (Configloader.INSTANCE.getReactionroleConfig(guild, event.getTextChannel(), event.getMessageId()) != null) {
				
				TextChannel channel = event.getTextChannel();
				String msgid = event.getMessageId();
				String[] actions = Configloader.INSTANCE.getReactionroleConfig(guild, channel, msgid).split(";");
				for (int i = 0; i < actions.length; i++) {
					String[] temp1 = actions[i].split("_");
					if (temp1[0].contains(event.getReactionEmote().getAsCodepoints())) {
						guild.addRoleToMember(event.getMember(), guild.getRoleById(temp1[1])).queue();
					}
				}
			}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		if(!event.isFromGuild()) {
			return;
		}
		//if reaction on reactionrole message, process reaction
		if (Configloader.INSTANCE.getReactionroleConfig(event.getGuild(), event.getTextChannel(), event.getMessageId()) != null) {
			Guild guild = event.getGuild();
			TextChannel channel = event.getTextChannel();
			String msgid = event.getMessageId();
			String[] actions = Configloader.INSTANCE.getReactionroleConfig(guild, channel, msgid).split(";");
			for (int i = 0; i < actions.length; i++) {
				String[] temp1 = actions[i].split("_");
				if (temp1[0].contains(event.getReactionEmote().getAsCodepoints())) {
					guild.removeRoleFromMember(event.getMember(), guild.getRoleById(temp1[1])).queue();
				}
			}
		}
	}
	
	public void addPollAnswer(String msgid, String emojiUnicode, Guild guild, User user) {
		String currentusers = Configloader.INSTANCE.getPollConfig(guild, msgid, "users");
		if (currentusers.contains(user.getId())) {
			return;
		}
		String[] old = Configloader.INSTANCE.getPollConfig(guild, msgid, "answercount").split(";");
		String[] temp1 = emojiUnicode.split("U");
		int choice = Integer.parseInt(temp1[1])-31;
		String current = String.valueOf(Integer.parseInt(old[choice])+1);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < old.length; i++) {
			if (i != choice) {
				sb.append(old[i]);
			} else {
				sb.append(current);
			}
			if (i+1 != old.length) {
				sb.append(";");
			}
		}
		Configloader.INSTANCE.setPollConfig(guild, msgid, "answercount", sb.toString());
		if (currentusers.equals("")) {
			Configloader.INSTANCE.setPollConfig(guild, msgid, "users", user.getId() + "_" + String.valueOf(choice));
		} else {
			Configloader.INSTANCE.setPollConfig(guild, msgid, "users", currentusers + ";" + user.getId() + "_" + String.valueOf(choice));
		}
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
		new Thread(() -> {
			Configcheck.INSTANCE.checkGuildConfigs(event.getGuild());
		}).start();
		if (event.getChannelType().isAudio()) {
			String id = event.getChannel().getId();
			Guild guild = event.getGuild();
			if (Configloader.INSTANCE.getGuildConfig(guild, "j2cs").contains(id)) {
				String[] entries = Configloader.INSTANCE.getGuildConfig(guild, "j2cs").split(";");
				for (int i = 0; i < entries.length; i++) {
					if (entries[i].contains(id)) {
						Configloader.INSTANCE.deleteGuildConfig(guild, "j2cs", entries[i]);
					}
				}
				return;
			}
		}
	}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		new Thread(() -> {
			Configcheck.INSTANCE.checkGuildConfigs(event.getGuild());
		}).start();
	}
}