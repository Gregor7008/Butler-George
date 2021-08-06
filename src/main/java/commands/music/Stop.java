package commands.music;

import commands.Commands;
import components.Answer;
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
			new Answer("/commands/music/stop:notconnected", event);
			return;
		}
		if (member.getVoiceState().inVoiceChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				new Answer("/commands/music/stop:nopermission", event);
				return;
			}
		} else {
			new Answer("/commands/music/stop:nopermission", event);
			return;
		}
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		new Answer("/commands/music/stop:stopped", event);
	}
}
