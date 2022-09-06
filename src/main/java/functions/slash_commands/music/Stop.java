package functions.slash_commands.music;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.functions.GuildMusicManager;
import engines.functions.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Stop implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Member member = event.getMember();
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "notconnected")).queue();
			return;
		}
		if (member.getVoiceState().inAudioChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission")).queue();
				return;
			}
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission")).queue();
			return;
		}
		Stop.stopandleave(guild);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "stopped")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("stop", "Stops the currently playing music!");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	public static  void stopandleave(Guild guild) {
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		VoiceChannel vc = (VoiceChannel) guild.getSelfMember().getVoiceState().getChannel();
		guild.getAudioManager().closeAudioConnection();
		if (vc.getUserLimit() != 0) {
			vc.getManager().setUserLimit(vc.getUserLimit() - 1).queue();
		}
	}
}