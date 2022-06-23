package commands.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.SubOperationData;
import components.utilities.ResponseDetector;
import net.dv8tion.jda.api.EmbedBuilder;
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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import operations.OperationList;

public class Configure implements Command {
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		
		ConcurrentHashMap<String, OperationData> operations = new ConcurrentHashMap<String, OperationData>();
		SelectMenu.Builder menuBuilder1 = SelectMenu.create("selVal").setRequiredRange(1, 1).setPlaceholder("Select a value");
		EmbedBuilder eb1 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selop").convert());
		
		if (event.getSubcommandName().equals("server")) {
			if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
				new OperationList().operations.forEach((name, operationRequest) -> {
					OperationData data = operationRequest.initialize();
					if (data.getCategory().equals(OperationData.ADMINISTRATION)) {
						operations.put(name, data);
						menuBuilder1.addOption(name, name);
						eb1.addField("`" + name + "`", data.getInfo(), true);
					}
				});
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, null, "nopermission").convert()).queue();
				return;
			}
		} else if (event.getSubcommandName().equals("user")) {
			new OperationList().operations.forEach((name, operationRequest) -> {
				OperationData data = operationRequest.initialize();
				if (data.getCategory().equals(OperationData.MODERATION)) {
					operations.put(name, data);
					menuBuilder1.addOption(name, name);
					eb1.addField("`" + name + "`", data.getInfo(), true);
				}
			});
		}
		
		Message msg = event.replyEmbeds(eb1.build()).addActionRow(menuBuilder1.build()).complete().retrieveOriginal().complete();
		ResponseDetector.waitForMenuSelection(guild, user, msg, menuBuilder1.getId(),
				e -> {
					OperationData data = operations.get(e.getSelectedOptions().get(0).getValue());
					SubOperationData[] subOperations = data.getSubOperations();
					if (subOperations != null) {
						List<Button> buttons = new ArrayList<>();
						EmbedBuilder eb2 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selsub").convert());
						for (int i = 0; i < subOperations.length; i++) {
							buttons.add(Button.primary(String.valueOf(i), subOperations[i].getName()));
							eb2.addField("`" + subOperations[i].getName() + "`", subOperations[i].getInfo(), true);
						}
						msg.editMessageEmbeds(eb2.build()).setActionRow(buttons).queue();
						ResponseDetector.waitForButtonClick(guild, user, msg, null,
								s -> {
									data.getOperationEventHandler().execute(new OperationEvent(event.getMember(), msg, subOperations[Integer.valueOf(s.getButton().getId())]));
								});
					} else {
						data.getOperationEventHandler().execute(new OperationEvent(event.getMember(), msg, null));
					}
				});
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
		} else if (role != null) {
			return member.getRoles().contains(role);
		} else {
			return false;
		}
	}
}