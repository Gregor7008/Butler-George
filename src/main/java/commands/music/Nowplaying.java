package commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Nowplaying implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/nowplaying:notconnected").convert()).queue();
			return;
		}
		if(member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/music/nowplaying:nopermission").convert()).queue();
			return;
		}
		final AudioTrackInfo info = PlayerManager.getInstance().getMusicManager(guild).audioPlayer.getPlayingTrack().getInfo();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/music/nowplaying:success").replaceDescription("{track}",  ":arrow_right: | `" + info.title + "` by `" + info.author + "`!").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("nowplaying", "Shows you information about the currently playing track!");
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
}