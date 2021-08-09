package base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import commands.Command;
import commands.CommandList;
import components.AnswerEngine;
import components.Automatic;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Processor extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		//routine check on Roles etc.
		new Automatic(event);
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		//assign Autoroles
		String autorolesraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "autoroles");
		if (autorolesraw != "") {
			String[] autoroles = autorolesraw.split(";");
			for (int i = 1; i <= autoroles.length; i++) {
				event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(autoroles[i-1]));
			}
		}
		//send Welcomemessage
		String welcomemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
		if (welcomemsgraw != "") {
			String[] welcomemsg = welcomemsgraw.split(";");
			welcomemsg[0].replace("{servername}", event.getGuild().getName());
			welcomemsg[0].replace("{membername}", event.getMember().getAsMention());
			welcomemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
			event.getGuild().getTextChannelById(welcomemsg[1]).sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(null, welcomemsg[0]));
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		//send goodbyemessage
		String goodbyemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
		if (goodbyemsgraw != "") {
			String[] goodbyemsg = goodbyemsgraw.split(";");
			goodbyemsg[0].replace("{servername}", event.getGuild().getName());
			goodbyemsg[0].replace("{membername}", event.getMember().getAsMention());
			goodbyemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
			event.getGuild().getTextChannelById(goodbyemsg[1]).sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(null, goodbyemsg[0]));
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
				newchannel.addMemberPermissionOverride(event.getMember().getIdLong(), allow, null);
			}
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
				clua.addCommands(cmd.initialize(guild));
			}
			clua.queue();
		}
	}
}
