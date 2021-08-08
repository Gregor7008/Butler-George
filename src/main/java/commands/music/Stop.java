package commands.music;

import commands.Commands;
import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Stop implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		final Member self = event.getGuild().getSelfMember();
		final Member member = event.getMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		if (!self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/stop:notconnected", event).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				AnswerEngine.getInstance().fetchMessage("/commands/music/stop:nopermission", event).queue();
				return;
			}
		} else {
			AnswerEngine.getInstance().fetchMessage("/commands/music/stop:nopermission", event).queue();
			return;
		}
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		event.getGuild().getAudioManager().closeAudioConnection();
		AnswerEngine.getInstance().fetchMessage("/commands/music/stop:stopped", event).queue();
	}
}
