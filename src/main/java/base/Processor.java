package base;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import components.AnswerEngine;
import components.Automatic;
import components.Test;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import textcommands.moderation.Autorole;
import textcommands.moderation.Clear;
import textcommands.moderation.Rolesorting;
import textcommands.music.Nowplaying;
import textcommands.music.Play;
import textcommands.music.Queue;
import textcommands.music.Skip;
import textcommands.music.Stop;
import textcommands.utilities.Embed;

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
			
			switch(command[1]) {
			//Utilities
				case("embed"):
					new Embed(event.getGuild(), event.getMember(), event.getChannel());
					break;
					
			//Moderation
				case("role-check"):
					new Rolesorting(event.getGuild(), event.getMember(), event.getChannel());
					break;
				case("clear"):
					new Clear(event.getGuild(), event.getMember(), event.getChannel(), argument);
					break;
				case("autorole"):
					new Autorole(event.getGuild(), event.getMember(), event.getChannel(), event.getMessage().getMentionedRoles().get(0), argument);
					break;
					
			//Music
				case("play"):
					new Play(event.getGuild(), event.getMember(), event.getChannel(), argument);
					break;
				case("stop"):
					new Stop(event.getGuild(), event.getMember(), event.getChannel());
					break;
				case("skip"):
					new Skip(event.getGuild(), event.getMember(), event.getChannel());
					break;
				case("nowplaying"):
					new Nowplaying(event.getGuild(), event.getMember(), event.getChannel());
					break;
				case("queue"):
					new Queue(event.getGuild(), event.getMember(), event.getChannel());
					break;
			
			//Developement
				case("shutdown"):
					event.getMessage().delete().queue();
					Bot.INSTANCE.shutdown();
					break;
				case("test"):
					new Test(event, argument);
					break;
				default:
					AnswerEngine.getInstance().fetchMessage("/base/processor:unknown", event.getGuild(), event.getMember(), event.getChannel()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
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
	
	@Override
	public void onSlashCommand(SlashCommandEvent event) {
		
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		CommandListUpdateAction commands = event.getJDA().getGuildById("die Discord Server ID").updateCommands();
		commands.addCommands(
							 new CommandData("dctest", "Nur ein Test für einen Slashcommand")
							 				 .addOptions(new OptionData(OptionType.STRING, "Option1", "Die erste Option des ersten Subcommands").setRequired(true)));
		commands.queue();
	}
}
