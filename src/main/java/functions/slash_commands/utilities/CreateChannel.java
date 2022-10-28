package functions.slash_commands.utilities;

import java.util.Collection;
import java.util.LinkedList;

import org.json.JSONArray;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CreateChannel implements SlashCommandEventHandler {
	
	@Override
	public void execute(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		String name = event.getOption("name").getAsString();
		this.createTextChannel(guild, user, name);
		event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("createchannel", "Creates a custom channel for you and your friends!")
									  .addOption(OptionType.STRING, "name", "The name of the new channel", true);
		command.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
		   	   .setGuildOnly(true);
		return command;
	}
	
	private void createTextChannel(Guild guild, User user, String name) {
		Collection<Permission> perms = this.setupPerms();
		Category cgy;
		if (ConfigLoader.INSTANCE.getMemberConfig(guild, user).getLong("customchannelcategory") == 0) {
			cgy = guild.createCategory(user.getName() + "'s channels").complete();
			cgy.upsertPermissionOverride(guild.getPublicRole()).setDenied(Permission.VIEW_CHANNEL).queue();
			cgy.upsertPermissionOverride(guild.getMember(user)).setAllowed(perms).queue();
			ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("customchannelcategories").put(cgy.getId(), user.getIdLong());
			if (!ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("customchannelaccessroles").isEmpty()) {
				JSONArray defroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("customchannelaccessroles");
				for (int i = 0; i < defroles.length(); i++) {
					cgy.upsertPermissionOverride(guild.getRoleById(defroles.getLong(i))).setAllowed(Permission.ALL_PERMISSIONS).queue();
				}
			}
			ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("customchannelcategory", cgy.getIdLong());
  	    } else {
  	    	cgy = guild.getCategoryById(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getLong("customchannelcategory"));
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