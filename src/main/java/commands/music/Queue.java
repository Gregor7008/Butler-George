package commands.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import commands.Commands;
import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Queue implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		final Member member = event.getMember();
		final Member self = event.getGuild().getSelfMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		final BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;
		if (!self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:notconnected", event).queue();
			return;
		}
		if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:nopermission", event).queue();
			return;
		}
		if (queue.isEmpty()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/nowplaying:noqueue", event).queue();
			return;
		}
		
		final int trackCount = Math.min(queue.size(), 20);
		final List<AudioTrack> trackList = new ArrayList<>(queue);
		final StringBuilder sB = new StringBuilder();
		
		for (int i = 0; i < trackCount; i++) {
			final AudioTrack track = trackList.get(i);
			final AudioTrackInfo info = track.getInfo();
			
			sB.append('#')
			  .append(String.valueOf(i+1))
			  .append(" `")
			  .append(info.title)
			  .append("` by `")
			  .append(info.author)
			  .append("`[")
			  .append(formatTime(track.getDuration()));
		   if (i+1 != trackCount) {
			   sB.append("]\n");
		   } else {
			   sB.append("]");
		   }
		}
		if(trackList.size() > trackCount) {
			sB.append("And")
			  .append(String.valueOf(trackList.size() - trackCount))
			  .append("` more...");
		}
		AnswerEngine.getInstance().buildMessage("Current queue:", sB.toString(), event.getChannel()).queue();
	}

	private String formatTime(long timeInMillis) {
		final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
		final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
		final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

}
