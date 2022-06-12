package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Move implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (ConfigLoader.run.getGuildConfig(guild).getLong("supporttalk") == 0) {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/move:nochannel").convert()).queue();
			return;
		}
		if (!guild.getMember(event.getOption("member").getAsUser()).getVoiceState().inAudioChannel()) {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/move:memnotconn").convert()).queue();
			return;
		}
		long vcid = ConfigLoader.run.getGuildConfig(guild).getLong("supporttalk");
		VoiceChannel st = guild.getVoiceChannelById(vcid);
		if (st == null) {
			ConfigLoader.run.getGuildConfig(guild).put("supporttalk", 0L);
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/move:nochannel").convert()).queue();
			return;
		}
		if (st.getMembers().contains(event.getMember())) {
			guild.moveVoiceMember(guild.getMember(event.getOption("member").getAsUser()), st).queue();
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/move:success").convert()).queue();
		} else {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/move:notconnected").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("move", "Move a member into the support talk").addOption(OptionType.USER, "member", "The member you want to move", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/move:help");
	}
}