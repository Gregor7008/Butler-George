package commands.music;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import components.commands.GuildMusicManager;
import components.commands.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Skip implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final Member member = event.getMember();
		final User user = event.getUser();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		final AudioPlayer audioPlayer = musicManager.audioPlayer;
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "notconnected").convert()).queue();
			return;
		}
		if (member.getVoiceState().inAudioChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission").convert()).queue();
				return;
			}
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission").convert()).queue();
			return;
		}
		if (audioPlayer.getPlayingTrack() == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noneplaying").convert()).queue();
			return;
		}
		musicManager.scheduler.nextTrack();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "skipped").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("skip", "Skips the currently playing track!");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
}