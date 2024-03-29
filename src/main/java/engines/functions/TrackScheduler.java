package engines.functions;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import assets.functions.AudioPlayerCache;
import engines.base.Toolbox;

public class TrackScheduler extends AudioEventAdapter {

	public final AudioPlayer player;
	public final BlockingQueue<AudioTrack> queue;

	public TrackScheduler(AudioPlayer aplayer) {
		player = aplayer;
		queue = new LinkedBlockingQueue<>();
	}
	
	public void queue(AudioTrack track) {
		if (!this.player.startTrack(track, true)) {
			this.queue.offer(track);
		}
	}
	
	public void nextTrack() {
	    AudioTrack next = this.queue.poll();
		if (next != null) {
			this.player.startTrack(next, false);
		} else {
			Toolbox.stopMusicAndLeaveOn(AudioPlayerCache.getInstance().getGuild(player));
		}
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			this.nextTrack();
		}
	}
}