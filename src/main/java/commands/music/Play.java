package commands.music;

import java.net.MalformedURLException;
import java.net.URL;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.music.GuildMusicManager;
import components.commands.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Play implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		final TextChannel channel = event.getTextChannel();
		final String argument = event.getOption("title").getAsString();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		if (argument == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/music/play:wrongusage").convert()).queue();
			return;
		}
		if (!member.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/music/play:noVCdefined").convert()).queue();
			return;
		}
		if (self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/music/play:alreadyinuse").convert()).queue();
			return;
		}
		this.load(event, argument, musicManager, channel, member);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("play", "Adds a new track to your music queue!").addOptions(new OptionData(OptionType.STRING, "title", "Hand over the title or the direct URL of your track!", true));
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
	
	private void load(SlashCommandInteractionEvent event, String argument, GuildMusicManager musicManager, TextChannel channel, Member member) {
		VoiceChannel vc = (VoiceChannel) member.getVoiceState().getChannel();
		channel.getGuild().getAudioManager().openAudioConnection(vc);
		if (!event.getGuild().getAudioManager().isConnected()) {
			if (vc.getUserLimit() != 0) {
				vc.getManager().setUserLimit(vc.getUserLimit() + 1).queue();
			}
		}
		if (!this.isURL(argument)) {
			String term = "ytsearch:" + argument;
			musicManager.scheduler.player.setVolume(15);
			PlayerManager.getInstance().loadAndPlay(event, term);
		} else {
			musicManager.scheduler.player.setVolume(15);
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
}