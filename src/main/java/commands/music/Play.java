package commands.music;

import java.net.MalformedURLException;
import java.net.URL;

import commands.Commands;
import components.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Play implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		final TextChannel channel = event.getChannel();
		final Member member = event.getMember();
		final Member self = event.getGuild().getSelfMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		if (argument == null) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/play:wrongusage", event).queue();
			return;
		}
		if (member.getVoiceState().getChannel() == self.getVoiceState().getChannel()) {
			this.load(argument, musicManager, channel, member);
			return;
		}
		if (self.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/play:alreadyinuse", event).queue();
			return;
		}
		if (!member.getVoiceState().inVoiceChannel()) {
			AnswerEngine.getInstance().fetchMessage("/commands/music/play:noVCdefined", event).queue();
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
