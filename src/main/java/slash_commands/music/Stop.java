package slash_commands.music;

import java.util.List;

import base.engines.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import slash_commands.assets.CommandEventHandler;
import slash_commands.engines.GuildMusicManager;
import slash_commands.engines.PlayerManager;

public class Stop implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Member member = event.getMember();
		final Guild guild = event.getGuild();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		if (!self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "notconnected").convert()).queue();
			return;
		}
		if (member.getVoiceState().inAudioChannel()) {
			if (member.getVoiceState().getChannel() != self.getVoiceState().getChannel()) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission").convert()).queue();
				return;
			}
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission").convert()).queue();
			return;
		}
		Stop.stopandleave(guild);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "stopped").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("stop", "Stops the currently playing music!");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
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