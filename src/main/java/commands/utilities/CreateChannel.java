package commands.utilities;

import java.util.Collection;
import java.util.LinkedList;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CreateChannel implements Command{

	private Collection<Permission> perms = this.setupPerms();
	
	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		String name = event.getOption("name").getAsString();
		if (Configloader.INSTANCE.getGuildConfig(guild, "ccrole").equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/createchannel:norole")).queue();
			return;
		}
		if (!event.getMember().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "ccrole")))) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/createchannel:nopermission")).queue();
			return;
		}
		this.createTextChannel(guild, user, name);
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/createchannel:success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("createchannel", "Creates a custom channel for you and your friends!")
				.addOption(OptionType.STRING, "name", "The name of the new channel", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/utilities/createchannel:help");
	}
	
	private void createTextChannel(Guild guild, User user, String name) {
		Category cgy;
		if (Configloader.INSTANCE.getUserConfig(guild, user, "cccategory").equals("")) {
			cgy = guild.createCategory(user.getName() + "'s channels").complete();
			cgy.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
			cgy.putPermissionOverride(guild.getMember(user)).setAllow(perms).queue();
			cgy.upsertPermissionOverride(guild.getMember(user)).deny(Permission.MESSAGE_MENTION_EVERYONE).queue();
			Configloader.INSTANCE.addGuildConfig(guild, "ccctgies", cgy.getId());
			if (!Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").equals("")) {
				String[] defroles = Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").split(";");
				for (int i = 0; i < defroles.length; i++) {
					cgy.putPermissionOverride(guild.getRoleById(defroles[i])).setAllow(Permission.ALL_PERMISSIONS).queue();
				}
			}
			Configloader.INSTANCE.setUserConfig(guild, user, "cccategory", cgy.getId());
  	    } else {
  	    	cgy = guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory"));
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