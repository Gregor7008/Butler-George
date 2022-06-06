package commands.moderation;

import org.json.JSONArray;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CustomChannelRoles implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray ccroles = ConfigLoader.run.getGuildConfig(guild).getJSONArray("customchannelroles");
		long roleID = event.getOption("role").getAsRole().getIdLong();
		if (event.getSubcommandName().equals("set")) {
			ccroles.clear();
			ccroles.put(roleID);
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/customchannelroles:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			ccroles.put(roleID);
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/customchannelroles:addsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			ConfigLoader.run.removeValueFromArray(ccroles, roleID);
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/customchannelroles:remsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("clear")) {
			ccroles.clear();
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/customchannelroles:clearsuccess").convert()).queue();
			return;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("customchannelroles", "0")
				.addSubcommands(new SubcommandData("set", "Sets the role that should be able to create custom user channels").addOption(OptionType.ROLE, "role", "The wanted role", true))
				.addSubcommands(new SubcommandData("add", "Enables the role to create custom user channels").addOption(OptionType.ROLE, "role", "The wanted role", true))
				.addSubcommands(new SubcommandData("remove", "Disables creation of custom user channels for the role").addOption(OptionType.ROLE, "role", "The wanted role", true))
				.addSubcommands(new SubcommandData("clear", "Disables creation of custom user channels for all roles"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/customchannelroles:help");
	}
}