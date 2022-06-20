package commands.utilities;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.utilities.WebhookEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Webhook implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		if (!event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/webhook:nopermission").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		WebhookEngine we = new WebhookEngine(event.getOption("link").getAsString());
		we.setContent(event.getOption("message").getAsString());
		try {
			we.execute();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/webhook:success").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
		} catch (IOException | IllegalArgumentException e) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/webhook:elink").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("webhook", "Sends a message to a webhook")
										.addOption(OptionType.STRING, "link", "The link of the webhook", true)
										.addOption(OptionType.STRING, "message", "The message that should be sent", true);
		return command;
	}
}