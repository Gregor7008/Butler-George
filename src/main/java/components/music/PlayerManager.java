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

import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
			PlayerCache.getInstance().addEntry(guildMusicManager.audioPlayer, guild);
			return guildMusicManager;
		});
	}
	
	public static PlayerManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlayerManager();
		}
		return INSTANCE;
	}
	
	public void loadAndPlay(SlashCommandInteractionEvent event, String trackURL ) {
		TextChannel channel = event.getTextChannel();
		final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
		this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
			
			@Override
			public void trackLoaded(AudioTrack track) {
				musicManager.scheduler.queue(track);
				event.replyEmbeds(AnswerEngine.ae.buildMessage("Success!",":white_check_mark: | `" + track.getInfo().title + "` by `" + track.getInfo().author + "` was added to the queue!")).queue();
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				final List<AudioTrack> tracks = playlist.getTracks();
				if (!playlist.isSearchResult()) {
					for (final AudioTrack track : tracks) {
						musicManager.scheduler.queue.add(track);
					}
					event.replyEmbeds(AnswerEngine.ae.buildMessage("Success!",":white_check_mark: | The playlist `" + playlist.getName() + "` with `" + String.valueOf(tracks.size()) + "` tracks was added to the queue!")).queue();
					return;
				} else {
					AudioTrack track = tracks.get(0);
					musicManager.scheduler.queue(track);
					event.replyEmbeds(AnswerEngine.ae.buildMessage("Success!",":white_check_mark: | `" + track.getInfo().title + "` by `" + track.getInfo().author + "` was added to the queue!")).queue();
				}
			}
			
			@Override
			public void noMatches() {
				event.replyEmbeds(AnswerEngine.ae.buildMessage("Error!",":x: | I'm sorry, but I couldn't find anything for your search term!")).queue();
				channel.getGuild().getAudioManager().closeAudioConnection();
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				event.replyEmbeds(AnswerEngine.ae.buildMessage("Error!",":x: | I'm sorry, but I couldn't load your song...")).queue();
				channel.getGuild().getAudioManager().closeAudioConnection();
			}
		});
	}	
}
