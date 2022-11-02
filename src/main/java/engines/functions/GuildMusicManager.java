package engines.functions;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
	
	public final AudioPlayer audioPlayer;
	public final TrackScheduler scheduler;
	private final AudioPlayerSendHandler sendHandler;
	
	public GuildMusicManager(AudioPlayerManager manager) {
		this.audioPlayer = manager.createPlayer();
		this.scheduler = new TrackScheduler(audioPlayer);
		this.audioPlayer.addListener(scheduler);
		this.sendHandler = new AudioPlayerSendHandler(audioPlayer);
	}
	
	public AudioPlayerSendHandler getSendHandler() {
		return sendHandler;
	}
}