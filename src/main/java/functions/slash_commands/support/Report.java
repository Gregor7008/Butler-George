package functions.slash_commands.support;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Report implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("modinbox") == 0) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nochannel")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyy");
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl());
		eb.setFooter(OffsetDateTime.now().format(formatter));
		eb.setColor(56575);
		if (event.getOption("link") == null) {
			eb.setTitle("Report for the user \"" + event.getOption("user").getAsUser().getName() + "\"");
		} else {
			eb.setTitle("Report of the user \"" + event.getOption("user").getAsUser().getName() + "\"", event.getOption("link").getAsString());
		}
		eb.setDescription(event.getOption("reason").getAsString());
		guild.getTextChannelById(ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("modinbox")).sendMessageEmbeds(eb.build()).queue();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("report", "Report a member for a specific reason")
									  .addOption(OptionType.USER, "user", "The user to be reported", true)
									  .addOption(OptionType.STRING, "reason", "The reason for your report", true)
									  .addOption(OptionType.STRING, "link", "The link to a message for evidence", false);
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
}