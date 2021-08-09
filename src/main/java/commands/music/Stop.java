package commands.music;

import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Stop {

	public Stop(Guild guild, Member imember, TextChannel channel) {
		final Member self = guild.getSelfMember();
		final Member member = imember;
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		if (!self.getVoiceState().inVoiceChannel()) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:notconnected", guild, member)).queue();
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:nopermission", guild, member)).queue();
				return;
			}
		} else {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:nopermission", guild, member)).queue();
			return;
		}
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		guild.getAudioManager().closeAudioConnection();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/stop:stopped", guild, member)).queue();
	}
}
