package commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import commands.Command;
import components.base.AnswerEngine;
import components.music.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Nowplaying implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		if (!self.getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/nowplaying:notconnected")).queue();
			return;
		}
		if(member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/music/nowplaying:nopermission")).queue();
			return;
		}
		final AudioTrackInfo info = PlayerManager.getInstance().getMusicManager(guild).audioPlayer.getPlayingTrack().getInfo();
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user, "/commands/music/nowplaying:success"), ":arrow_right: | `" + info.title + "` by `" + info.author + "`!")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("nowplaying", "Shows you information about the currently playing track!");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/music/nowplaying:help");
	}

}