package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DefaultAccessRoles implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:nopermission")).queue();
			return;
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:sendroles")).queue();
		Bot.INSTANCE.getWaiter().waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getTextChannel().getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {List<Role> roles = e.getMessage().getMentionedRoles();
					  for (int i = 0; i < roles.size(); i++) {
						  Configloader.INSTANCE.setGuildConfig(guild, "ccdefaccess", roles.get(i).getId());
					  }
					  event.getTextChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:success")).queue();},
				2, TimeUnit.MINUTES,
				() -> {event.getTextChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("defaultaccessroles", "0")
				.addSubcommands(new SubcommandData("set", "Sets the roles that should have access to user channels by default on this server"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/defaultaccessroles:help");
	}

}
