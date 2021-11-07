package base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import commands.Command;
import commands.CommandList;
import commands.utilities.Suggest;
import components.base.AnswerEngine;
import components.base.Configloader;
import components.moderation.ModController;
import components.moderation.ModMail;
import components.moderation.NoLimitsOnly;
import components.utilities.LevelEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Processor extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		//ModController
		new Thread (() -> {
			new ModController().modcheck();
		}).start();
		//levelsystem
		LevelEngine.getInstance().messagereceived(event);
		//automoderation
			//-->in developement
		//Anonymous ModMail
		if (Configloader.INSTANCE.getMailConfig1(event.getChannel().getName()) != null) {
			if (event.getAuthor().isBot()) {
				return;
			}
			PrivateChannel pc = Bot.INSTANCE.jda.openPrivateChannelById(Configloader.INSTANCE.getMailConfig1(event.getChannel().getName())).complete();
			pc.sendMessage(event.getMessage().getContentDisplay()).queue();
			return;
		}
		//Suggestions
		String suggestid = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "suggest");
		if (suggestid != null && event.getChannel().getId().equals(suggestid) && !event.getAuthor().isBot()) {
			new Suggest().sendsuggestion(event.getGuild(), event.getMember(), event.getMessage().getContentRaw());
			event.getMessage().delete().queue();
		}
	}
	@Override
	public void onReady(ReadyEvent event) {
		//initialize Slash-Commands
		for (int i = 0; i < event.getJDA().getGuilds().size(); i++) {
			Guild guild = event.getJDA().getGuilds().get(i);
			CommandListUpdateAction clua = guild.updateCommands();
			CommandList commandList = new CommandList();
			List<String> commandnames = new ArrayList<>();
			commandnames.addAll(commandList.commands.keySet());
			for (int e = 0; e < commandnames.size(); e++) {
				Command cmd = commandList.commands.get(commandnames.get(e));
				clua.addCommands(cmd.initialize());
			}
			clua.queue();
		}
	}
	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		if (Configloader.INSTANCE.getGuildConfig(event.getGuild(), "ignored").contains(event.getChannel().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/base/processor:ignoredchannel")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		//perform Slash-Command
		CommandList commandList = new CommandList();
		Command cmd;
		if ((cmd = commandList.commands.get(event.getName())) != null) {
			cmd.perform(event);
		}
		//levelsystem
		LevelEngine.getInstance().slashcommand(event);
	}
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		//assign Autoroles
		if (event.getMember().getUser().isBot()) {
			String botautorolesraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "botautoroles");
			if (!botautorolesraw.equals("")) {
				String[] botautoroles = botautorolesraw.split(";");
				for (int i = 0; i < botautoroles.length; i++) {
					event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(botautoroles[i])).queue();
				}
			}
		} else {
			String autorolesraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "autoroles");
			if (!autorolesraw.equals("")) {
				String[] autoroles = autorolesraw.split(";");
				for (int i = 0; i < autoroles.length; i++) {
					event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(autoroles[i])).queue();
				}
			}
			//send Welcomemessage
			String welcomemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (!welcomemsgraw.equals("")) {
				String[] welcomemsg = welcomemsgraw.split(";");
				welcomemsg[0].replace("{servername}", event.getGuild().getName());
				welcomemsg[0].replace("{membername}", event.getMember().getAsMention());
				welcomemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
				welcomemsg[0].replace("{date}", currentdate);
				event.getGuild().getTextChannelById(welcomemsg[1]).sendMessage(welcomemsg[0]).queue();
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
			goodbyemsg[0].replace("{servername}", event.getGuild().getName());
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
		this.managej2cleave(event.getGuild(), event.getMember(), event.getChannelLeft());
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
		this.managej2cleave(event.getGuild(), event.getMember(), event.getChannelLeft());
	}
	
	private void managej2cjoin(Guild guild, Member member, VoiceChannel channeljoined) {
		//check for Join2create-channel & create User-channel if true
		String j2cid = Configloader.INSTANCE.getGuildConfig(guild, "join2create");
		if (channeljoined.getId().equals(j2cid)) {
			ChannelAction<VoiceChannel> newchannel = guild.createVoiceChannel(member.getEffectiveName() + "'s channel", channeljoined.getParent());
			Collection<Permission> perms = new LinkedList<Permission>();
			perms.add(Permission.MANAGE_CHANNEL);
			perms.add(Permission.MANAGE_PERMISSIONS);
			perms.add(Permission.CREATE_INSTANT_INVITE);
			perms.add(Permission.VOICE_MUTE_OTHERS);
			perms.add(Permission.VOICE_SPEAK);
			VoiceChannel nc = newchannel.addMemberPermissionOverride(member.getIdLong(), perms, null).complete();
			guild.moveVoiceMember(member, nc).queue();
			Configloader.INSTANCE.addGuildConfig(guild, "j2cs", nc.getId() + "-" + member.getUser().getId());
		}
	}
	
	private void managej2cleave(Guild guild, Member member, VoiceChannel channelleft) {
		//check if VoiceChannelLeft was a Userchannel
		VoiceChannel vc = channelleft;
		if (Configloader.INSTANCE.getGuildConfig(guild, "j2cs").contains(vc.getId())) {
			if (vc.getMembers().size() == 0) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "j2cs", vc.getId() + "-" + member.getUser().getId());
				vc.delete().queue();
			} else {
				if (Configloader.INSTANCE.getGuildConfig(guild, "j2cs").contains(vc.getId() + "-" + member.getUser().getId())) {
					Collection<Permission> perms = new LinkedList<Permission>();
					perms.add(Permission.MANAGE_CHANNEL);
					perms.add(Permission.MANAGE_PERMISSIONS);
					perms.add(Permission.CREATE_INSTANT_INVITE);
					perms.add(Permission.VOICE_MUTE_OTHERS);
					perms.add(Permission.VOICE_SPEAK);
					Configloader.INSTANCE.deleteGuildConfig(guild, "j2cs", vc.getId() + "-" + member.getUser().getId());
					Member newowner =  vc.getMembers().get(0);
					Configloader.INSTANCE.addGuildConfig(guild, "j2cs", vc.getId() + "-" + newowner.getUser().getId());
					vc.getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(member).setName(newowner.getEffectiveName() + "'s channel").queue();
				}
			}
		}
	}
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}		
		//process modmail
		new ModMail(event);
	}
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		//check if it was on a poll, then call up "Poll.addAnswer(event);"
		//also implement reactionrole support
	}
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		new Thread (() -> {
			new NoLimitsOnly().noliRolecheck();
		}).start();
	}
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		new Thread (() -> {
			new NoLimitsOnly().noliRolecheck();
		}).start();
	}
}
