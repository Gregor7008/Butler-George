package functions.slash_commands.administration;

import java.util.ArrayList;
import java.util.List;

import assets.base.AwaitTask;
import assets.functions.ConfigurationEvent;
import assets.functions.ConfigurationOptionData;
import assets.functions.ConfigurationSubOptionData;
import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import functions.configuration_options.ServerConfigurationOptionsList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

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
				e -> {
					ConfigurationOptionData data = ServerConfigurationOptionsList.getConfigurationOptionData(e.getSelectedOptions().get(0).getValue());
					List<Permission> requiredBotPermissions = data.getConfigurationEventHandler().getRequiredPermissions();
					boolean insufficientPermissions = false;
					StringBuilder sB = new StringBuilder();
					for (int i = 0; i < requiredBotPermissions.size(); i++) {
						sB.append(requiredBotPermissions.get(i).getName().toLowerCase());
						if (i + 1 != requiredBotPermissions.size()) {
							sB.append(", ");
						}
						if (!event.getGuild().getSelfMember().hasPermission(requiredBotPermissions.get(i))) {
							insufficientPermissions = true;
						}
					}
					if (!event.getMember().hasPermission(data.getRequiredPermissions())) {
						e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "nopermission"));
					} else if (insufficientPermissions && !event.getMember().isOwner() && !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
						e.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "insufficientperms").replaceDescription("{permissions}", sB.toString())).queue();
					} else {
						ConfigurationSubOptionData[] subOperations = data.getSubOptions();
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
										s.editMessageEmbeds(s.getMessage().getEmbeds()).setComponents().queue(onCompletion -> {
											data.getConfigurationEventHandler().execute(new ConfigurationEvent(event.getMember(), s, subOperations[Integer.valueOf(s.getButton().getId())]));
										});
									}).append();	
						} else {
							data.getConfigurationEventHandler().execute(new ConfigurationEvent(event.getMember(), e, null));
						}
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
}