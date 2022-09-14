package engines.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import assets.functions.AudioPlayerCache;
import engines.base.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PlayerManager {
	
	private static PlayerManager INSTANCE;
	private final Map<Long, GuildMusicManager> musicManagers;
	private final AudioPlayerManager audioPlayerManager;
	
	public PlayerManager() {
		musicManagers = new HashMap<>();
		audioPlayerManager = new DefaultAudioPlayerManager();		
		AudioSourceManagers.registerRemoteSources(audioPlayerManager);
		AudioSourceManagers.registerLocalSource(audioPlayerManager);
	}
	
	public GuildMusicManager getMusicManager(Guild guild) {
		return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildID) -> {
			final GuildMusicManager guildMusicManager = new GuildMusicManager(audioPlayerManager);
			guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
			AudioPlayerCache.getInstance().addEntry(guildMusicManager.audioPlayer, guild);
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
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		TextChannel channel = guild.getTextChannelById(event.getMessageChannel().getIdLong());
		final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
		this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
			
			@Override
			public void trackLoaded(AudioTrack track) {
				musicManager.scheduler.queue(track);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "track")
						.replaceDescription("{track}", track.getInfo().title)
						.replaceDescription("{author}", track.getInfo().author)).queue();
				System.out.println("trackLoaded1");
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				final List<AudioTrack> tracks = playlist.getTracks();
				if (!playlist.isSearchResult()) {
					for (final AudioTrack track : tracks) {
						musicManager.scheduler.queue.add(track);
					}
					event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list")
							.replaceDescription("{name}", playlist.getName())
							.replaceDescription("{author}", String.valueOf(playlist.getTracks().size()))).queue();
					System.out.println("playlistLoaded");
				} else {
					AudioTrack track = tracks.get(0);
					musicManager.scheduler.queue(track);
					event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "track")
							.replaceDescription("{track}", track.getInfo().title)
							.replaceDescription("{author}", track.getInfo().author)).queue();
					System.out.println("trackLoaded2");
				}
			}
			
			@Override
			public void noMatches() {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nomatch")).queue();
				channel.getGuild().getAudioManager().closeAudioConnection();
				System.out.println("noMatches");
			}
			
			@Override
			public void loadFailed(FriendlyException exception) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fail")).queue();
				channel.getGuild().getAudioManager().closeAudioConnection();
				System.out.println("loadFailed");
			}
		});
	}	
}