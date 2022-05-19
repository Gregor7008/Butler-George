package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Join2Create implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final String id = event.getOption("channel").getAsGuildChannel().getId();
		if (guild.getVoiceChannelById(id) == null) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/join2create:invalid").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			if (ConfigLoader.run.getGuildConfig(guild, "join2create").contains(id)) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/join2create:adderror").convert()).queue();
			} else {
				ConfigLoader.run.addGuildConfig(guild, "join2create", id);
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/join2create:addsuccess").convert()).queue();
			}
		}
		if (event.getSubcommandName().equals("remove")) {
			if (ConfigLoader.run.getGuildConfig(guild, "join2create").contains(id)) {
				ConfigLoader.run.removeGuildConfig(guild, "join2create", id);
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/join2create:remsuccess").convert()).queue();
			} else {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/join2create:remerror").convert()).queue();
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("join2create", "0")
										.addSubcommands(new SubcommandData("add", "Adds a new join2create channel")
												.addOptions(new OptionData(OptionType.CHANNEL, "channel", "Enter the voicechannel that should be used as the join2create channel!", true)))
										.addSubcommands(new SubcommandData("remove", "Removes a join2create channel")
												.addOptions(new OptionData(OptionType.CHANNEL, "channel", "Enter the voicechannel that should be used as the join2create channel!", true)));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/join2create:help");
	}
}