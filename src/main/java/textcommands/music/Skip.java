package textcommands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Skip{

	public Skip(Guild guild, Member imember, TextChannel channel) {
		final Member self = guild.getSelfMember();
		final Member member = imember;
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		final AudioPlayer audioPlayer = musicManager.audioPlayer;
		if (!self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/skip:notconnected", guild, member, channel).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				AnswerEngine.getInstance().fetchMessage("/commands/music/skip:nopermission", guild, member, channel).queue();
				return;
			}
		} else {
			AnswerEngine.getInstance().fetchMessage("/commands/music/skip:nopermission", guild, member, channel).queue();
			return;
		}
		if (audioPlayer.getPlayingTrack() == null) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/skip:noneplaying", guild, member, channel).queue();
			return;
		}
		musicManager.scheduler.nextTrack();
		AnswerEngine.getInstance().fetchMessage("/commands/music/skip:skipped", guild, member, channel).queue();
	}

}
