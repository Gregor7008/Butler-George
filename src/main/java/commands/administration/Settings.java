package commands.administration;

import java.util.concurrent.ConcurrentHashMap;

import actions.ActionList;
import components.actions.ActionData;
import components.base.ConfigLoader;
import components.commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Settings implements Command {
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
//		final Guild guild = event.getGuild();
//		final User user = event.getUser();
		
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
//		Ask user to choose mod/admin (Button)
//		On button click dispose other collection
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
}