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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Levelreward implements Command{

	private Guild guild;
	private User user;
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		if (event.getSubcommandName().equals("add")) {
			ConfigLoader.run.addGuildConfig(guild, "levelrewards", event.getOption("role").getAsRole().getId() + "_" + event.getOption("level").getAsString());
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/levelreward:addsuccess")
					.replaceDescription("{role}", event.getOption("role").getAsRole().getAsMention())
					.replaceDescription("{level}", event.getOption("level").getAsString()).convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			String rawinput = ConfigLoader.run.getGuildConfig(guild, "levelrewards");
			if (rawinput.equals("")) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/levelreward:norewards").convert()).queue();
				return;
			}
			String[] rewards = rawinput.split(";");
			for (int i = 0; i < rewards.length; i++) {
				if (rewards[i].contains(event.getOption("role").getAsRole().getId())) {
					ConfigLoader.run.removeGuildConfig(guild, "levelrewards", rewards[i]);
					String[] reward = rewards[i].split("_");
					event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/levelreward:addsuccess")
							.replaceDescription("{role}", guild.getRoleById(reward[0]).getAsMention())
							.replaceDescription("{level}", reward[1]).convert()).queue();
					return;
				}
			}
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listrewards(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("levelreward", "0")
									.addSubcommands(new SubcommandData("add", "Adds a new levelreward in form of a role")
											.addOption(OptionType.ROLE, "role", "The roles that should be given", true)
											.addOption(OptionType.INTEGER, "level", "The level on which the role should be given to a user", true))
									.addSubcommands(new SubcommandData("remove", "Removes an existing levelreward").addOption(OptionType.ROLE, "role", "The rewarded role that should be removed", true))
									.addSubcommands(new SubcommandData("list", "Lists all existing levelrewards"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/levelreward:help");
	}
	
	private void listrewards(SlashCommandInteractionEvent event) {
		StringBuilder sB = new StringBuilder();
		String currentraw = ConfigLoader.run.getGuildConfig(guild, "levelrewards");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/levelreward:norewards").convert()).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] reward = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/levelreward:list").replaceDescription("{list}", "#1\s\s" + guild.getRoleById(reward[0]).getAsMention() + "\s->\s" + reward[1]).convert()).queue();
			return;
		}
		String[] current = currentraw.split(";");
		for (int i = 1; i <= current.length; i++) {
			String[] reward = current[i-1].split("_");
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i == current.length) {
				sB.append(guild.getRoleById(reward[0]).getAsMention() + "\s->\s" + reward[1]);
			} else {
				sB.append(guild.getRoleById(reward[0]).getAsMention() + "\s->\s" + reward[1] + "\n");
			}
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/levelreward:list").replaceDescription("{list}", sB.toString()).convert()).queue();
	}
}