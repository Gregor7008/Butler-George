package components.music;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import components.Answer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class PlayerManager {
	
	private static PlayerManager INSTANCE;
	private final Map<Long, GuildMusicManager> musicManagers;
	private final AudioPlayerManager audioPlayerManager;
	
	public PlayerManager() {
		musicManagers = new HashedMap<>();
		audioPlayerManager = new DefaultAudioPlayerManager();
		
		AudioSourceManagers.registerRemoteSources(audioPlayerManager);
		AudioSourceManagers.registerLocalSource(audioPlayerManager);
	}
	
	public GuildMusicManager getMusicManager(Guild guild) {
		return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildID) -> {
			final GuildMusicManager guildMusicManager = new GuildMusicManager(audioPlayerManager);
			guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
			return guildMusicManager;
		});
	}
	
	public static PlayerManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlayerManager();
		}
		return INSTANCE;
	}
	
	public void loadAndPlay(TextChannel channel, String trackURL ) {
		final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
		this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
			
			@Override
			public void trackLoaded(AudioTrack track) {
				musicManager.scheduler.queue(track);
				new Answer("Success!",":white_check_mark: | \"" + track.getInfo().title + "\" by \"" + track.getInfo().author + "\" was loaded succesfully!", channel);
				
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				final List<AudioTrack> tracks = playlist.getTracks();
				musicManager.scheduler.queue(tracks.get(0));
				new Answer("Success!",":white_check_mark: | \"" + tracks.get(0).getInfo().title + "\" by \"" + tracks.get(0).getInfo().author + "\" was loaded succesfully!", channel);
			}
			
			@Override
			public void noMatches() {
				new Answer("Fail!",":x: | I'm sorry, but I couldn't find anything for your search term!", channel);
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
