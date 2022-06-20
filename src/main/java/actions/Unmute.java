package actions;

import org.json.JSONObject;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.moderation.ModEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Unmute implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user =  event.getUser();
		final User cuser = event.getOption("user").getAsUser();
		JSONObject userconfig = ConfigLoader.getMemberConfig(guild, cuser);
		if (!userconfig.getBoolean("muted") && !userconfig.getBoolean("tempmuted")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/unmute:nomute").convert()).queue();
			return;
		}
		userconfig.put("muted", false);
		userconfig.put("tempmuted", false);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/unmute:success").convert()).queue();
		ModEngine.run.guildModCheck(guild);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("unmute", "Unmute a user").addOption(OptionType.USER, "user", "The user that should be unmuted", true);
		return command;
	}
}