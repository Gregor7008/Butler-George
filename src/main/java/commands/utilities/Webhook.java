package commands.utilities;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import commands.Command;
import components.base.AnswerEngine;
import components.utilities.WebhookEngine;
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
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/webhook:nopermission")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		WebhookEngine we = new WebhookEngine(event.getOption("link").getAsString());
		we.setContent(event.getOption("message").getAsString());
		try {
			we.execute();
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/webhook:success")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
		} catch (IOException | IllegalArgumentException e) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/webhook:elink")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("webhook", "Sends a message to a webhook")
										.addOption(OptionType.STRING, "link", "The link of the webhook", true)
										.addOption(OptionType.STRING, "message", "The message that should be sent", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/command/utilities/webhook:help");
	}
}