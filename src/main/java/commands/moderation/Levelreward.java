package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Levelreward implements Command{

	private Guild guild;
	private User user;
	
	@Override
	public void perform(SlashCommandEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/levelreward:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			Configloader.INSTANCE.addGuildConfig(guild, "levelrewards", event.getOption("role").getAsRole().getId() + "_" + event.getOption("level").getAsString());
			event.replyEmbeds(AnswerEngine.ae.buildMessage("Success!", ":white_check_mark: | The role\s"
							+ event.getOption("role").getAsRole().getAsMention() + "\s" + AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/levelreward:addsuccess") + "\s" + event.getOption("level").getAsString() + "!")).queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			String rawinput = Configloader.INSTANCE.getGuildConfig(guild, "levelrewards");
			if (rawinput.equals("")) {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/levelreward:norewards")).queue();
				return;
			}
			String[] rewards = rawinput.split(";");
			for (int i = 0; i < rewards.length; i++) {
				if (rewards[i].contains(event.getOption("role").getAsRole().getId())) {
					Configloader.INSTANCE.deleteGuildConfig(guild, "levelrewards", rewards[i]);
					String[] reward = rewards[i].split("_");
					event.replyEmbeds(AnswerEngine.ae.buildMessage("Success!", ":white_check_mark: | The role\s"
								+ guild.getRoleById(reward[0]).getAsMention() + "\s" + AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/levelreward:remsuccess") + "\s" + reward[1] + "!")).queue();
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
		CommandData command = new CommandData("levelreward", "0")
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
	
	private void listrewards(SlashCommandEvent event) {
		StringBuilder sB = new StringBuilder();
		String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "levelrewards");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/levelreward:norewards")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] reward = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.ae.buildMessage(AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/levelreward:list"), "#1\s\s" + guild.getRoleById(reward[0]).getAsMention() + "\s->\s" + reward[1])).queue();
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
		event.replyEmbeds(AnswerEngine.ae.buildMessage(AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/levelreward:list"), sB.toString())).queue();
	}
}