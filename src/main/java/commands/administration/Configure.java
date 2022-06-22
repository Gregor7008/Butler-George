package commands.administration;

import java.util.concurrent.ConcurrentHashMap;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import components.operation.OperationData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import operations.OperationList;

public class Configure implements Command {
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		
		ConcurrentHashMap<String, OperationData> operations = new ConcurrentHashMap<String, OperationData>();
		SelectMenu.Builder menuBuilder = SelectMenu.create("selVal").setRequiredRange(1, 1).setPlaceholder("Select a value");
		
		if (event.getSubcommandName().equals("server")) {
			new OperationList().operations.forEach((name, actionRequest) -> {
				OperationData data = actionRequest.initialize();
				if (data.getCategory().equals(OperationData.ADMINISTRATION)) {
					operations.put(name, data);
					menuBuilder.addOption(name, name);
				}
			});
		} else if (event.getSubcommandName().equals("user")) {
			new OperationList().operations.forEach((name, actionRequest) -> {
				OperationData data = actionRequest.initialize();
				if (data.getCategory().equals(OperationData.MODERATION)) {
					operations.put(name, data);
				}
			});
		}
		Message msg = event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selval").convert()).addActionRow(null).complete().retrieveOriginal().complete();
//		When admin: choose between SET, ADD, REMOVE or CLEAR (Further options later on).
//		Choose from (still available) commands and request input according to (Sub-) options, then execute
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("configure", "0")
									  .addSubcommands(new SubcommandData("server", "Configure channels, roles and auto actions for your server!"),
											  		  new SubcommandData("user", "Configure permissions, warnings and penalties for a user!"));
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		Role role = member.getGuild().getRoleById(ConfigLoader.getGuildConfig(member.getGuild()).getLong("modrole"));
		if (member.hasPermission(Permission.MANAGE_SERVER)) {
			return true;
		} else {
			return member.getRoles().contains(role);
		}
	}
}