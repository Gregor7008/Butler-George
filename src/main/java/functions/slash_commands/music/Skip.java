package functions.slash_commands.music;

import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.functions.GuildMusicManager;
import engines.functions.PlayerManager;
import engines.functions.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Skip implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final Member member = event.getMember();
		final User user = event.getUser();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		final AudioPlayer audioPlayer = musicManager.audioPlayer;
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "notconnected")).queue();
			return;
		}
		if (member.getVoiceState().inAudioChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "nopermission")).queue();
				return;
			}
		} else {
			event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "nopermission")).queue();
			return;
		}
		if (musicManager.scheduler.queue.isEmpty()) {
			event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "noskip")).queue();
			TrackScheduler.stopMusicAndLeaveOn(guild);
			return;
		}
		AudioTrack nextTrack = musicManager.scheduler.queue.poll();
		audioPlayer.stopTrack();
		audioPlayer.startTrack(nextTrack, false);
		event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "skipped")
				.replaceDescription("{track}", this.formatTrackInfo(guild, user, nextTrack))).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("skip", "Skips the currently playing track!");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	private String formatTrackInfo(Guild guild, User user, AudioTrack track) {
		final String temp1 = LanguageEngine.getRaw(guild, user, Queue.class, "list").split(";")[0];
		final AudioTrackInfo info = track.getInfo();
		return temp1.replace("{title}", info.title)
		  		    .replace("{author}", info.author)
		  		    .replace("{time}", this.formatTime(track.getDuration()));
	}

	private String formatTime(long timeInMillis) {
		final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
		final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
		final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}