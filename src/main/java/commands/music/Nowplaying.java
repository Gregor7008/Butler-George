package commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import commands.Commands;
import components.AnswerEngine;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Nowplaying implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		final Member member = event.getMember();
		final Member self = event.getGuild().getSelfMember();
		final AudioTrackInfo info = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo();
		if (!self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:notconnected", event).queue();
			return;
		}
		if(member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:nopermission", event).queue();
			return;
		}
		AnswerEngine.getInstance().buildMessage("Current track:", ":arrow_right: | `" + info.title + "` by `" + info.author + "`!", event.getChannel()).queue();
	}

}
