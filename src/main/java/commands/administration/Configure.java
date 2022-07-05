package commands.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import components.ResponseDetector;
import components.Toolbox;
import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import operations.AutoMessages;
import operations.AutoRoles;
import operations.InboxChannels;
import operations.Join2CreateChannels;
import operations.LevelRewards;
import operations.Penalties;
import operations.ReactionRoles;
import operations.StaticRoles;
import operations.SupportTalk;

public class Configure implements CommandEventHandler {
	
	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		
		ConcurrentHashMap<String, OperationData> operations = new ConcurrentHashMap<String, OperationData>();
		SelectMenu.Builder menuBuilder1 = SelectMenu.create("selVal").setRequiredRange(1, 1).setPlaceholder("Select a value");
		EmbedBuilder eb1 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selval").convert());
		
		new OperationList().operations.forEach((name, operationRequest) -> {
			OperationData data = operationRequest.initialize();
			operations.put(name, data);
			menuBuilder1.addOption(name, name);
			eb1.addField("`" + name + "`", data.getInfo(), true);
		});
		
		Message msg = event.replyEmbeds(eb1.build()).addActionRow(menuBuilder1.build()).complete().retrieveOriginal().complete();
		ResponseDetector.waitForMenuSelection(guild, user, msg, menuBuilder1.getId(),
				e -> {OperationData data = operations.get(e.getSelectedOptions().get(0).getValue());
					  SubOperationData[] subOperations = data.getSubOperations();
					  if (subOperations != null) {
						List<Button> buttons = new ArrayList<>();
						EmbedBuilder eb2 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selsub").convert());
						for (int i = 0; i < subOperations.length; i++) {
							buttons.add(Button.primary(String.valueOf(i), subOperations[i].getName()));
								eb2.addField("`" + subOperations[i].getName() + "`", subOperations[i].getInfo(), true);
						}
						e.editMessageEmbeds(eb2.build()).setActionRow(buttons).queue();
						ResponseDetector.waitForButtonClick(guild, user, msg, null,
								s -> {
									Toolbox.deleteActionRows(s.getMessage(),
											() -> {data.getOperationEventHandler().execute(new OperationEvent(event.getMember(), s, subOperations[Integer.valueOf(s.getButton().getId())]));});
								});
					} else {
						data.getOperationEventHandler().execute(new OperationEvent(event.getMember(), e, null));
					}
				});
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
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
	
	private static class OperationList {
			
		public ConcurrentHashMap<String, OperationEventHandler> operations = new ConcurrentHashMap<>();
		
		public OperationList() {
			//Administration
			this.operations.put("AutoRoles", new AutoRoles());
			this.operations.put("StaticRoles", new StaticRoles());
			this.operations.put("AutoMessages", new AutoMessages());
			this.operations.put("Join2CreateChannels", new Join2CreateChannels());
			this.operations.put("InboxChannels", new InboxChannels());
			this.operations.put("LevelRewards", new LevelRewards());
			this.operations.put("Penalties", new Penalties());
			this.operations.put("ReactionRoles", new ReactionRoles());
			this.operations.put("SupportTalk", new SupportTalk());
		}
	}
}