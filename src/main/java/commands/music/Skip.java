package commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import commands.Command;
import components.base.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Skip implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final Member member = event.getMember();
		final User user = event.getUser();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		final AudioPlayer audioPlayer = musicManager.audioPlayer;
		if (!self.getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/skip:notconnected")).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/skip:nopermission")).queue();
				return;
			}
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/skip:nopermission")).queue();
			return;
		}
		if (audioPlayer.getPlayingTrack() == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/skip:noneplaying")).queue();
			return;
		}
		musicManager.scheduler.nextTrack();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/skip:skipped")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("skip", "Skips the currently playing track!");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/music/skip:help");
	}
}