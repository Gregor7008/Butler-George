package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import components.moderation.PenaltyEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Close implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (ConfigLoader.removeValueFromArray(ConfigLoader.getGuildConfig(guild).getJSONArray("ticketchannels"), event.getTextChannel().getIdLong())) {
			event.getTextChannel().delete().queue();
			return;
		}
		if (ConfigLoader.getModMailOfChannel(event.getTextChannel().getId()) != null) {
			String cid = event.getTextChannel().getId();
			event.getTextChannel().delete().queue();
			User cuser = ConfigLoader.getModMailOfChannel(cid);
			ConfigLoader.getModMailOfChannel(cid).openPrivateChannel().complete().sendMessageEmbeds(
					AnswerEngine.fetchMessage(guild, cuser, "/commands/moderation/close:closed").replaceDescription("{reason}", event.getOption("reason").getAsString()).convert()).queue();
			ConfigLoader.getFirstGuildLayerConfig(guild, "modmails").remove(String.valueOf(ConfigLoader.getModMailOfChannel(cid)));
			try {
				if (event.getOption("warning").getAsBoolean()) {
					ConfigLoader.getMemberConfig(guild, cuser).getJSONArray("warnings").put("Modmail abuse");
					PenaltyEngine.run.guildCheck(guild);
				}
			} catch (NullPointerException e) {}
		} else {
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/moderation/close:nochannel").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("close", "Closes the support channel")
				.addOption(OptionType.STRING, "reason", "The reason why the ticket was closed", true)
				.addOption(OptionType.BOOLEAN, "warning", "Whether the member should be warned");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getRaw(guild, user, "/commands/moderation/close:help");
	}
}