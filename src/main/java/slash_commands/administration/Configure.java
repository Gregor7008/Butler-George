package slash_commands.administration;

import java.util.ArrayList;
import java.util.List;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import configuration_options.ServerConfigurationOptionsList;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import slash_commands.assets.SlashCommandEventHandler;

public class Configure implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		
		SelectMenu.Builder menuBuilder1 = SelectMenu.create("selVal").setRequiredRange(1, 1).setPlaceholder("Select a value");
		EmbedBuilder eb1 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selval"));
		
		if (event.getSubcommandName().equals("server")) {
			ServerConfigurationOptionsList.getConfigurationOptionData().forEach(data -> {
				String name = data.getName();
				menuBuilder1.addOption(name, name);
				eb1.addField("`" + name + "`", data.getInfo(), true);
			});
		}
		
		SelectMenu menu = menuBuilder1.build();
		Message msg = event.replyEmbeds(eb1.build()).addActionRow(menu).complete().retrieveOriginal().complete();
		AwaitTask.forSelectMenuInteraction(guild, user, msg,
				e -> {ConfigurationOptionData data = ServerConfigurationOptionsList.getConfigurationOptionData(e.getSelectedOptions().get(0).getValue());
					  ConfigurationSubOptionData[] subOperations = data.getSubOperations();
					  if (subOperations != null) {
						List<Button> buttons = new ArrayList<>();
						EmbedBuilder eb2 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selsub"));
						for (int i = 0; i < subOperations.length; i++) {
							buttons.add(Button.secondary(String.valueOf(i), subOperations[i].getName()));
								eb2.addField("`" + subOperations[i].getName() + "`", subOperations[i].getInfo(), true);
						}
						e.editMessageEmbeds(eb2.build()).setActionRow(buttons).queue();
						AwaitTask.forButtonInteraction(guild, user, msg,
								s -> {
									s.editMessageEmbeds(s.getMessage().getEmbeds()).setActionRows().queue(onCompletion -> {
										data.getOperationEventHandler().execute(new ConfigurationEvent(event.getMember(), s, subOperations[Integer.valueOf(s.getButton().getId())]));
									});
								}).append();
					} else {
						data.getOperationEventHandler().execute(new ConfigurationEvent(event.getMember(), e, null));
					}
				}).addValidComponents(menu.getId()).append();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("configure", "0")
									  .addSubcommands(new SubcommandData("server", "Configure channels, roles and auto actions for your server!"));
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
			   .setGuildOnly(true);
		return command;
	}
	
	@Override
	public boolean checkBotPermissions(SlashCommandInteractionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailableTo(Member member) {
		// TODO Auto-generated method stub
		return false;
	}
}