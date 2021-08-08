package commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import commands.Commands;
import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Skip implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		final Member self = event.getGuild().getSelfMember();
		final Member member = event.getMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		final AudioPlayer audioPlayer = musicManager.audioPlayer;
		if (!self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/skip:notconnected", event).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				AnswerEngine.getInstance().fetchMessage("/commands/music/skip:nopermission", event).queue();
				return;
			}
		} else {
			AnswerEngine.getInstance().fetchMessage("/commands/music/skip:nopermission", event).queue();
			return;
		}
		if (audioPlayer.getPlayingTrack() == null) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/skip:noneplaying", event).queue();
			return;
		}
		musicManager.scheduler.nextTrack();
		AnswerEngine.getInstance().fetchMessage("/commands/music/skip:skipped", event).queue();
	}

}
