package commands.music;

import commands.Command;
import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Stop implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Member member = event.getMember();
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		if (!self.getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:notconnected")).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:nopermission")).queue();
				return;
			}
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:nopermission")).queue();
			return;
		}
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		guild.getAudioManager().closeAudioConnection();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:stopped")).queue();
	}

	@Override
	public CommandData initialize(Guild guild) {
		CommandData command = new CommandData("stop", "Stop the currently playing music!");
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command, to stop music which is currently playing in your channel. This also clears the queue!";
	}
}
