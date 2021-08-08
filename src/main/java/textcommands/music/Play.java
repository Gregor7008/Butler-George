package textcommands.music;

import java.net.MalformedURLException;
import java.net.URL;

import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public class Play {

	public Play(Guild guild, Member member, TextChannel channel, String argument) {
		final Member self = guild.getSelfMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		if (argument == null) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/play:wrongusage", guild, member, channel).queue();
			return;
		}
		if (member.getVoiceState().getChannel() == self.getVoiceState().getChannel()) {
			this.load(argument, musicManager, channel, member);
			return;
		}
		if (self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/play:alreadyinuse", guild, member, channel).queue();
			return;
		}
		if (!member.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/play:noVCdefined", guild, member, channel).queue();
			return;
		}
		this.load(argument, musicManager, channel, member);
	}
	
	private void load(String argument, GuildMusicManager musicManager, TextChannel channel, Member member) {
		channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
		if (!isURL(argument)) {
			String term = "ytsearch:" + argument;
			musicManager.scheduler.player.setVolume(5);
			PlayerManager.getInstance().loadAndPlay(channel, term);
		} else {
			musicManager.scheduler.player.setVolume(5);
			PlayerManager.getInstance().loadAndPlay(channel, argument);
		}
	}
	
	private boolean isURL(String test) {
		try {
			new URL(test);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
}
