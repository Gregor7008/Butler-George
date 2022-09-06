package functions.slash_commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.functions.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Nowplaying implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "notconnected")).queue();
			return;
		}
		if(member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission")).queue();
			return;
		}
		final AudioTrackInfo info = PlayerManager.getInstance().getMusicManager(guild).audioPlayer.getPlayingTrack().getInfo();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, " | `" + info.title + "` by `" + info.author + "`!")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("nowplaying", "Shows you information about the currently playing track!");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
}