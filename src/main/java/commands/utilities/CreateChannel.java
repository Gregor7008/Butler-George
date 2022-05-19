package commands.utilities;

import java.util.Collection;
import java.util.LinkedList;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CreateChannel implements Command{
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		String name = event.getOption("name").getAsString();
		if (ConfigLoader.cfl.getGuildConfig(guild, "ccrole").equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/createchannel:norole").convert()).queue();
			return;
		}
		Role permrole = guild.getRoleById(ConfigLoader.cfl.getGuildConfig(guild, "ccrole"));
		if (!event.getMember().getRoles().contains(permrole) && !permrole.isPublicRole()) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/createchannel:nopermission").convert()).queue();
			return;
		}
		this.createTextChannel(guild, user, name);
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/createchannel:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("createchannel", "Creates a custom channel for you and your friends!")
				.addOption(OptionType.STRING, "name", "The name of the new channel", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/utilities/createchannel:help");
	}
	
	private void createTextChannel(Guild guild, User user, String name) {
		Collection<Permission> perms = this.setupPerms();
		Category cgy;
		if (ConfigLoader.cfl.getUserConfig(guild, user, "cccategory").equals("")) {
			cgy = guild.createCategory(user.getName() + "'s channels").complete();
			cgy.upsertPermissionOverride(guild.getPublicRole()).setDenied(Permission.VIEW_CHANNEL).queue();
			cgy.upsertPermissionOverride(guild.getMember(user)).setAllowed(perms).queue();
			ConfigLoader.cfl.addGuildConfig(guild, "ccctgies", cgy.getId());
			if (!ConfigLoader.cfl.getGuildConfig(guild, "ccdefaccess").equals("")) {
				String[] defroles = ConfigLoader.cfl.getGuildConfig(guild, "ccdefaccess").split(";");
				for (int i = 0; i < defroles.length; i++) {
					cgy.upsertPermissionOverride(guild.getRoleById(defroles[i])).setAllowed(Permission.ALL_PERMISSIONS).queue();
				}
			}
			ConfigLoader.cfl.setUserConfig(guild, user, "cccategory", cgy.getId());
  	    } else {
  	    	cgy = guild.getCategoryById(ConfigLoader.cfl.getUserConfig(guild, user, "cccategory"));
  	    }
		guild.createTextChannel(name, cgy).complete();
	}
	
	private LinkedList<Permission> setupPerms() {
		LinkedList<Permission> ll = new LinkedList<>();
		ll.add(Permission.VIEW_CHANNEL);
		ll.add(Permission.MANAGE_CHANNEL);
		ll.add(Permission.MANAGE_WEBHOOKS);
		Permission.getPermissions(Permission.ALL_TEXT_PERMISSIONS).forEach(e -> ll.add(e));
		Permission.getPermissions(Permission.ALL_VOICE_PERMISSIONS).forEach(e -> ll.add(e));
		return ll;
	}
}