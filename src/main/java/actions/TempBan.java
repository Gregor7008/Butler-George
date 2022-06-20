package actions;

import java.time.OffsetDateTime;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.moderation.ModEngine;
import components.base.ConfigLoader;
import components.base.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TempBan implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOption("member").getAsUser();
		OffsetDateTime until = OffsetDateTime.now().plusDays(Long.parseLong(event.getOption("days").getAsString()));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanned", true);
		guild.getMember(user).ban(0).queue();
		ModEngine.run.guildModCheck(guild);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/tempban:success")
				.replaceDescription("{user}", user.getName())
				.replaceDescription("{time}", event.getOption("days").getAsString()).convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("tempban", "Bans a user temporarily")
												.addOptions(new OptionData(OptionType.USER, "member", "The member you want to ban", true))
												.addOptions(new OptionData(OptionType.INTEGER, "days", "The number of days you want the member to be banned", true));
		return command;
	}	
}