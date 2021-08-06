package commands.music;

import java.net.URI;
import java.net.URISyntaxException;

import commands.Commands;
import components.Answer;
import components.music.GuildMusicManager;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Play implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		final TextChannel channel = event.getChannel();
		final Member member = event.getMember();
		final Member self = event.getGuild().getSelfMember();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
		if (argument == null) {
			new Answer("/commands/music/play:wrondusage", event);
			return;
		}
		if (!member.getVoiceState().inVoiceChannel()) {
			new Answer("/commands/music/play:noVCdefined", event);
			return;
		}
		if (self.getVoiceState().inVoiceChannel()) {
			new Answer("/commands/music/play:alreadyinuse", event);
			return;
		} else {
			final AudioManager audioManager = event.getGuild().getAudioManager();
			audioManager.openAudioConnection(member.getVoiceState().getChannel());
		}
		if (!isURL(argument)) {
			String term = "ytsearch;" + argument;
			musicManager.scheduler.player.setVolume(50);
			PlayerManager.getInstance().loadAndPlay(channel, term);
		} else {
			musicManager.scheduler.player.setVolume(50);
			PlayerManager.getInstance().loadAndPlay(channel, argument);
		}
	}
	
	private boolean isURL(String test) {
		try {
			new URI(test);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
