package commands.administration;

import java.util.concurrent.ConcurrentHashMap;

import actions.ActionList;
import components.actions.ActionData;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import components.utilities.ResponseDetector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Settings implements Command {
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		
		ConcurrentHashMap<String, ActionData> adminActions = new ConcurrentHashMap<String, ActionData>();
		ConcurrentHashMap<String, ActionData> modActions =  new ConcurrentHashMap<String, ActionData>();
		
		new ActionList().actions.forEach((name, actionRequest) -> {
			ActionData data = actionRequest.initialize();
			if (data.getCategory().equals(ActionData.ADMINISTRATION)) {
				adminActions.put(name, data);
			} else if (data.getCategory().equals(ActionData.MODERATION)) {
				modActions.put(name, data);
			}
		});
		
		Message msg = event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selcat").convert())
						   .addActionRow(Button.primary("admin", Emoji.fromMarkdown(":a:")),
								   	     Button.primary("mod", Emoji.fromMarkdown(":m:")))
						   .complete().retrieveOriginal().complete();
		ResponseDetector.waitForButtonClick(guild, user, msg, null,
				e -> {if (e.getButton().getId().equals("admin")) {
						  this.selectAction(guild, user, msg, adminActions);
					  } else if (e.getButton().getId().equals("mod")) {
						  this.selectAction(guild, user, msg, modActions);
					  }},
				() -> {msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});

//		When admin: choose between SET, ADD, REMOVE or CLEAR (Further options later on).
//		Choose from (still available) commands and request input according to (Sub-) options, then execute
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("settings", "The settings of your server");
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		if (member.hasPermission(Permission.MANAGE_SERVER)) {
			return true;
		}
		Role modRole = member.getGuild().getRoleById(ConfigLoader.getGuildConfig(member.getGuild()).getLong("modrole"));
		if (modRole == null) {
			return false;
		} else {
			return member.getRoles().contains(modRole);
		}
	}
	
	private void selectAction(Guild guild, User user, Message msg, ConcurrentHashMap<String, ActionData> actions) {
		SelectMenu.Builder menuBuild = SelectMenu.create("selAct")
												 .setRequiredRange(1, 1);
		actions.forEach((name, actionData) -> {
			menuBuild.addOption(name, name);
		});
		SelectMenu menu = menuBuild.build();
		msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selact").convert())
		   .setActionRow(menu).queue();
		ResponseDetector.waitForMenuSelection(guild, user, msg, menu.getId(),
				e -> {this.selectSubAction(guild, user, msg, actions.get(e.getSelectedOptions().get(0).getValue()));},
				() -> {msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, null, "timeout").convert()).queue();});
	}
	
	private void selectSubAction(Guild guild, User user, Message msg, ActionData selectedAction) {
		if (!guild.getMember(user).hasPermission(selectedAction.getMinimumPermission())) {
			msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopermission").convert()).queue();
			return;
		}
		if (selectedAction.getSubActions() != null) {
			String[] subActions = selectedAction.getSubActions();
			
		}
	}
}