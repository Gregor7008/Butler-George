package commands.administration;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import components.commands.WebhookBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Webhook implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		WebhookBuilder wB = new WebhookBuilder(event.getOption("link").getAsString());
		wB.setContent(event.getOption("message").getAsString());
		try {
			wB.execute();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
		} catch (IOException | IllegalArgumentException e) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "elink").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
		}
	}
	
	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("webhook", "Sends a message to a webhook")
										.addOption(OptionType.STRING, "link", "The link of the webhook", true)
										.addOption(OptionType.STRING, "message", "The message that should be sent", true);
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_WEBHOOKS))
		   	   .setGuildOnly(true);
		return command;
	}
}