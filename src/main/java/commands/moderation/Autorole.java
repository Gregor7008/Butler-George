package commands.moderation;

import base.Configloader;
import commands.Command;
import components.AnswerEngine;
import components.Developers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Autorole implements Command {

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		if (!member.hasPermission(Permission.MANAGE_ROLES) && !Developers.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			final Role role = event.getOption("addrole").getAsRole();
			Configloader.INSTANCE.addGuildConfig(guild, "autoroles", role.getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:addsuccess")).queue();;
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			final Role role = event.getOption("removerole").getAsRole();
			Configloader.INSTANCE.deleteGuildConfig(guild, "autoroles", role.getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:removesuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			final StringBuilder sB = new StringBuilder();
			final String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "autoroles");
			if (currentraw.equals("")) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:noautoroles")).queue();;
				return;
			}
			if (!currentraw.contains(";")) {
				event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new member joins:", "#1\s\s" + guild.getRoleById(currentraw).getAsMention())).queue();
				return;
			}
			String[] current = currentraw.split(";");
			for (int i = 1; i <= current.length; i++) {
				sB.append('#')
				  .append(String.valueOf(i) + "\s\s");
				if (i == current.length) {
					sB.append(guild.getRoleById(current[i-1]).getAsMention());
				} else {
					sB.append(guild.getRoleById(current[i-1]).getAsMention() + "\n");
				}
			}
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new member joins:", sB.toString())).queue();
			return;
		}
	}

	@Override
	public CommandData initialize(Guild guild) {
		CommandData command = new CommandData("autorole", "Configurates the roles given to every new member joining in the future!")
								  .addSubcommands(new SubcommandData("add", "Adds a new role!").addOptions(new OptionData(OptionType.ROLE, "addrole", "Mention the role you want to add!").setRequired(true)))
								  .addSubcommands(new SubcommandData("remove", "Removes a role!").addOptions(new OptionData(OptionType.ROLE, "removerole", "Mention the role you want to remove!").setRequired(true)))
								  .addSubcommands(new SubcommandData("list", "Displays the roles currently given to every new member!"));
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to configure, which roles should be given each new joining member!";
	}

}
