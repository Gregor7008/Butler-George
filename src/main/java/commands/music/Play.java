package commands.music;

import java.net.MalformedURLException;
import java.net.URL;

import commands.Command;
import components.base.AnswerEngine;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Play implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		final TextChannel channel = event.getTextChannel();
		final String argument = event.getOption("title").toString();
		final Member self = guild.getSelfMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		if (argument == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/play:wrongusage")).queue();
			return;
		}
		if (member.getVoiceState().getChannel() == self.getVoiceState().getChannel()) {
			this.load(event, argument, musicManager, channel, member);
			return;
		}
		if (self.getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/play:alreadyinuse")).queue();
			return;
		}
		if (!member.getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/music/play:noVCdefined")).queue();
			return;
		}
		this.load(event, argument, musicManager, channel, member);
	}
	
	private void load(SlashCommandEvent event, String argument, GuildMusicManager musicManager, TextChannel channel, Member member) {
		channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
		if (!isURL(argument)) {
			String term = "ytsearch:" + argument;
			musicManager.scheduler.player.setVolume(5);
			PlayerManager.getInstance().loadAndPlay(event, term);
		} else {
			musicManager.scheduler.player.setVolume(10);
			PlayerManager.getInstance().loadAndPlay(event, argument);
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

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("play", "Adds a new track to your music queue!").addOptions(new OptionData(OptionType.STRING, "title", "Hand over the title or the direct URL of your track!"));
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to start playing music in your voicechannel or to add a new track to your queue, which is going to be played one by one in your channel!";
	}
}
