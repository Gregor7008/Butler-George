package actions.moderation;

import java.time.OffsetDateTime;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.ConfigManager;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class TempBan implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getOptionAsUser(0);
		OffsetDateTime until = OffsetDateTime.now().plusDays(event.getOptionAsLong(1));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanned", true);
		guild.getMember(user).ban(0).queue();
		ModEngine.run.guildModCheck(guild);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/tempban:success")
				.replaceDescription("{user}", user.getName())
				.replaceDescription("{time}", String.valueOf(event.getOptionAsLong(1)))).queue();
	}

	@Override
	public ActionData initialize() {
		//TODO Initialize TempBan
		return null;
	}	
}