package commands.music;

import commands.Command;
import components.base.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Stop implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Member member = event.getMember();
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/music/stop:notconnected")).queue();
			return;
		}
		if (member.getVoiceState().inAudioChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/music/stop:nopermission")).queue();
				return;
			}
		} else {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/music/stop:nopermission")).queue();
			return;
		}
		this.stopandleave(guild);
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/music/stop:stopped")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("stop", "Stops the currently playing music!");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/music/stop:help");
	}
	
	public void stopandleave(Guild guild) {
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		VoiceChannel vc = (VoiceChannel) guild.getSelfMember().getVoiceState().getChannel();
		guild.getAudioManager().closeAudioConnection();
		if (vc.getUserLimit() != 0) {
			vc.getManager().setUserLimit(vc.getUserLimit() - 1).queue();
		}
	}
}