package base;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import commands.CommandList;
import commands.Commands;
import components.AnswerEngine;
import components.Automatic;
import components.Test;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

public class Processor extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String argument;
		if (event.getMessage().getAuthor().isBot()) {return;}
		if (event.getMessage().getContentRaw().contains(Bot.INSTANCE.getBotConfig("prefix"))) {			
			String[] raw = event.getMessage().getContentRaw().split("\\s+", 2);
			String[] command = raw[0].split(Bot.INSTANCE.getBotConfig("prefix"));
			try {argument = raw[1];
			} catch (Exception e) {argument = "";}
			
			CommandList commandList = new CommandList();
			Commands mdc;
			if((mdc = commandList.CommandList.get(command[1])) != null) {
				mdc.perform(event, argument);
				return;
			}
			
			switch(command[1]) {
				case("shutdown"):
					event.getMessage().delete().queue();
					Bot.INSTANCE.shutdown();
					break;
				case("test"):
					new Test(event, argument);
					break;
				default:
					AnswerEngine.getInstance().fetchMessage("/base/processor:unknown", event).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
			}
		} else { 
			new Automatic(event);
		}
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
			AnswerEngine.getInstance().buildMessage(null, welcomemsg[0], event.getGuild().getTextChannelById(welcomemsg[1]));
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
			AnswerEngine.getInstance().buildMessage(null, goodbyemsg[0], event.getGuild().getTextChannelById(goodbyemsg[1]));
		}
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
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
}
