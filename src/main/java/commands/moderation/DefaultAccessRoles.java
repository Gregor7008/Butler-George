package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DefaultAccessRoles implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:sendroles").convert()).queue();
		Bot.INSTANCE.getWaiter().waitForEvent(MessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getTextChannel().getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {List<Role> roles = e.getMessage().getMentionedRoles();
					  for (int i = 0; i < roles.size(); i++) {
						  ConfigLoader.cfl.setGuildConfig(guild, "ccdefaccess", roles.get(i).getId());
					  }
					  event.getTextChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:success").convert()).queue();},
				2, TimeUnit.MINUTES,
				() -> {event.getTextChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("defaultaccessroles", "0")
				.addSubcommands(new SubcommandData("set", "Sets the roles that should have access to user channels by default on this server"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/defaultaccessroles:help");
	}

}
