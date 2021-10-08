package base;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import commands.Command;
import commands.CommandList;
import components.base.AnswerEngine;
import components.base.Configloader;
import components.moderation.ModMail;
import components.utilities.LevelEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Processor extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		//finishing setup
		if (event.getMessage().getMentionedMembers().contains(event.getGuild().getSelfMember()) && Configloader.INSTANCE.getGuildConfig(event.getGuild(), "modrole").equals("")) {
			Configloader.INSTANCE.setGuildConfig(event.getGuild(), "modrole", event.getMessage().getMentionedRoles().get(0).getId());
			event.getGuild().getOwner().getUser().openPrivateChannel().queue((channel) -> {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Thanks for setting me up!", ":white_check_mark: | Setup is completed!\n Your modrole is: " + 
																				  event.getGuild().getRoleById(Configloader.INSTANCE.getGuildConfig(event.getGuild(), "modrole"))));
			});
		}
		//levelsystem
		LevelEngine.getInstance().messagereceived(event);
		//automoderation
		//-->in developement
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
		Configloader.INSTANCE.findorCreateUserConfig(event.getGuild(), event.getUser());
		//assign Autoroles
		if (event.getMember().getUser().isBot()) {
			String botautorolesraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "botautoroles");
			if (botautorolesraw != "") {
				String[] botautoroles = botautorolesraw.split(";");
				for (int i = 1; i <= botautoroles.length; i++) {
					event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(botautoroles[i-1]));
				}
			}
		} else {
			String autorolesraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "autoroles");
			if (autorolesraw != "") {
				String[] autoroles = autorolesraw.split(";");
				for (int i = 1; i <= autoroles.length; i++) {
					event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(autoroles[i-1]));
				}
			}
		}
		//send Welcomemessage
		String welcomemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
		String currentdate = date.format(formatter);
		if (welcomemsgraw != "") {
			String[] welcomemsg = welcomemsgraw.split(";");
			welcomemsg[0].replace("{servername}", event.getGuild().getName());
			welcomemsg[0].replace("{membername}", event.getMember().getAsMention());
			welcomemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
			welcomemsg[0].replace("{date}", currentdate);
			event.getGuild().getTextChannelById(welcomemsg[1]).sendMessage(welcomemsg[0]).queue();
		}
	}
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		//send goodbyemessage
		String goodbyemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
		String currentdate = date.format(formatter);
		if (goodbyemsgraw != "") {
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
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		//check for Join2create-channel & create User-channel if true
		String j2cid = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "join2create");
		if (j2cid != "") {
			if (event.getChannelJoined().getId() == j2cid) {
				ChannelAction<VoiceChannel> newchannel = event.getGuild().createVoiceChannel(event.getMember().getEffectiveName() + "'s channel!", event.getChannelJoined().getParent());
				Collection<Permission> allow = new LinkedList<Permission>();
				allow.add(Permission.MANAGE_CHANNEL);
				allow.add(Permission.MANAGE_PERMISSIONS);
				allow.add(Permission.CREATE_INSTANT_INVITE);
				allow.add(Permission.VOICE_MUTE_OTHERS);
				allow.add(Permission.VOICE_SPEAK);
				newchannel.addMemberPermissionOverride(event.getMember().getIdLong(), allow, null).queue();
				event.getGuild().moveVoiceMember(event.getMember(), event.getGuild().getVoiceChannelsByName(event.getMember().getEffectiveName() + "'s channel!", true).get(0)).queue();
			}
		}
		//levelsystem
		LevelEngine.getInstance().voicejoin(event);
	}
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		//check if VoiceChannelLeft was a Userchannel
		if (event.getChannelLeft().getName().contains(event.getMember().getEffectiveName() + "'s channel!")) {
			event.getChannelLeft().delete().queue();
		}
	}
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		try {
		event.getGuild().getOwner().getUser().openPrivateChannel().queue((channel) -> {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Thanks for inviting me!", ":exclamation: | To finish my setup, please mention me on your server as well as the role that should be able to warn members!\n Thanks :heart:")).queue();
		});} catch (Exception e) {}
		Configloader.INSTANCE.findorCreateGuildConfig(event.getGuild());
	}
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		new ModMail(event);
	}
}
