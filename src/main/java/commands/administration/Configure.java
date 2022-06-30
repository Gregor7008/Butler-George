package commands.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import context.Mute;
import context.TempBan;
import context.TempMute;
import context.Unmute;
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
import operations.AutoRoles;
import operations.CustomChannelRoles;
import operations.DefaultAccessRoles;
import operations.Goodbye;
import operations.Inbox;
import operations.Join2Create;
import operations.LevelReward;
import operations.Penalty;
import operations.ReactionRole;
import operations.SupportTalk;
import operations.Welcome;

public class Configure implements Command {
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		
		ConcurrentHashMap<String, OperationData> operations = new ConcurrentHashMap<String, OperationData>();
		SelectMenu.Builder menuBuilder1 = SelectMenu.create("selVal").setRequiredRange(1, 1).setPlaceholder("Select a value");
		EmbedBuilder eb1 = new EmbedBuilder(LanguageEngine.fetchMessage(guild, user, this, "selval").convert());
		
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
						//TODO Rework of Replyable.java needed, as message instance contains ActionRows saved which will be reloaded when using this instance.
						//Also the below handed over Message instance "msg" has to be changed to the new Message instance created in line 91!
						ResponseDetector.waitForButtonClick(guild, user, msg, null,
								s -> {
									data.getOperationEventHandler().execute(new OperationEvent(event.getMember(), msg, subOperations[Integer.valueOf(s.getButton().getId())]));
								});
					} else {
						data.getOperationEventHandler().execute(new OperationEvent(event.getMember(), e.getMessage(), null));
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
		Role role = member.getGuild().getRoleById(ConfigLoader.getGuildConfig(member.getGuild()).getLong("moderationrole"));
		if (member.hasPermission(Permission.MANAGE_SERVER)) {
			return true;
		} else if (role != null) {
			return member.getRoles().contains(role);
		} else {
			return false;
		}
	}
	
	private static class OperationList {
			
		public ConcurrentHashMap<String, OperationEventHandler> operations = new ConcurrentHashMap<>();
		
		public OperationList() {
			//Administration
			this.operations.put("AutoRole", new AutoRoles());
			this.operations.put("CustomChannelRoles", new CustomChannelRoles());
			this.operations.put("DefaultAccessRoles", new DefaultAccessRoles());
			this.operations.put("Goodbye", new Goodbye());
			this.operations.put("Join2Create", new Join2Create());
			this.operations.put("CommunityInbox", new Inbox());
			this.operations.put("LevelRewards", new LevelReward());
			this.operations.put("Penalty", new Penalty());
			this.operations.put("ReactionRole", new ReactionRole());
			this.operations.put("SupportTalk", new SupportTalk());
			this.operations.put("Welcome", new Welcome());
			//Moderation
			this.operations.put("Mute", new Mute());
			this.operations.put("TempBan", new TempBan());
			this.operations.put("TempMute", new TempMute());
			this.operations.put("Unmute", new Unmute());
		}
	}
}