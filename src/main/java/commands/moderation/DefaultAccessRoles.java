package commands.moderation;

import org.json.JSONArray;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DefaultAccessRoles implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray ccdefroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelaccessroles");
		long roleID = event.getOption("role").getAsRole().getIdLong();
		if (event.getSubcommandName().equals("set")) {
			ccdefroles.clear();
			ccdefroles.put(roleID);
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			ccdefroles.put(roleID);
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:addsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			ConfigLoader.removeValueFromArray(ccdefroles, roleID);
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:remsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("clear")) {
			ccdefroles.clear();
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:clearsuccess").convert()).queue();
			return;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("defaultaccessroles", "0")
				.addSubcommands(new SubcommandData("set", "Sets the role that should have access to user channels by default"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getRaw(guild, user, "/commands/moderation/defaultaccessroles:help");
	}

}
