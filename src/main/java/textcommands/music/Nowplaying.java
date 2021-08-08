package textcommands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import components.AnswerEngine;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Nowplaying{

	public Nowplaying(Guild guild, Member member, TextChannel channel) {
		final Member self = guild.getSelfMember();
		final AudioTrackInfo info = PlayerManager.getInstance().getMusicManager(guild).audioPlayer.getPlayingTrack().getInfo();
		if (!self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:notconnected", guild, member, channel).queue();
			return;
		}
		if(member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:nopermission", guild, member, channel).queue();
			return;
		}
		AnswerEngine.getInstance().buildMessage("Current track:", ":arrow_right: | `" + info.title + "` by `" + info.author + "`!", channel).queue();
	}

}
