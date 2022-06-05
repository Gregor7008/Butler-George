package commands.moderation;

import org.json.JSONArray;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BotAutoRole implements Command{

	private Guild guild;
	private User user;
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		if (event.getSubcommandName().equals("add")) {
			Role role = event.getOption("addrole").getAsRole();
			ConfigLoader.run.getGuildConfig(guild).getJSONArray("botautoroles").put(role.getIdLong());
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user,"/commands/moderation/botautorole:addsuccess").convert()).queue();;
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			Role role = event.getOption("removerole").getAsRole();
			ConfigLoader.run.removeValueFromArray(ConfigLoader.run.getGuildConfig(guild).getJSONArray("botautoroles"), role.getIdLong());
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user,"/commands/moderation/botautorole:removesuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listroles(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("botautorole", "0")
								  .addSubcommands(new SubcommandData("add", "Adds a role, that'll be given to every new member joining!")
										  .addOptions(new OptionData(OptionType.ROLE, "addrole", "Mention the role you want to add!", true)))
								  .addSubcommands(new SubcommandData("remove", "Removes a role, that was previously given to every new member joining!")
										  .addOptions(new OptionData(OptionType.ROLE, "removerole", "Mention the role you want to remove!", true)))
								  .addSubcommands(new SubcommandData("list", "Displays the roles currently given to every new member joining!"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.run.getRaw(guild, user, "/commands/moderation/botautorole:help");
	}
	
	private void listroles(SlashCommandInteractionEvent event) {
		StringBuilder sB = new StringBuilder();
		JSONArray botautoroles = ConfigLoader.run.getGuildConfig(guild).getJSONArray("botautoroles");
		if (botautoroles.isEmpty()) {
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user,"/commands/moderation/botautorole:nobotautoroles").convert()).queue();;
			return;
		}
		for (int i = 0; i < botautoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == botautoroles.length()) {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/botautorole:list").replaceDescription("{list}", sB.toString()).convert()).queue();
	}
}