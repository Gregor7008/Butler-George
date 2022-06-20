package commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.music.GuildMusicManager;
import components.commands.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Skip implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final Member member = event.getMember();
		final User user = event.getUser();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		final AudioPlayer audioPlayer = musicManager.audioPlayer;
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/skip:notconnected").convert()).queue();
			return;
		}
		if (member.getVoiceState().inAudioChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/skip:nopermission").convert()).queue();
				return;
			}
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/skip:nopermission").convert()).queue();
			return;
		}
		if (audioPlayer.getPlayingTrack() == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/skip:noneplaying").convert()).queue();
			return;
		}
		musicManager.scheduler.nextTrack();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/skip:skipped").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("skip", "Skips the currently playing track!");
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
}