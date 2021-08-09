package commands.music;

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
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/skip:notconnected", guild, member)).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/skip:nopermission", guild, member)).queue();
				return;
			}
		} else {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/skip:nopermission", guild, member)).queue();
			return;
		}
		if (audioPlayer.getPlayingTrack() == null) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/skip:noneplaying", guild, member)).queue();
			return;
		}
		musicManager.scheduler.nextTrack();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/skip:skipped", guild, member)).queue();
	}

}
