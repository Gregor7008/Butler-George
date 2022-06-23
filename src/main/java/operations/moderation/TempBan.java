package operations.moderation;

import java.time.OffsetDateTime;

import components.base.ConfigLoader;
import components.base.ConfigManager;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class TempBan implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOptionAsUser(0);
		OffsetDateTime until = OffsetDateTime.now().plusDays(event.getOptionAsLong(1));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanned", true);
		guild.getMember(user).ban(0).queue();
		ModEngine.run.guildModCheck(guild);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
				.replaceDescription("{user}", user.getName())
				.replaceDescription("{time}", String.valueOf(event.getOptionAsLong(1)))).queue();
	}

	@Override
	public OperationData initialize() {
		//TODO Initialize TempBan
		return null;
	}	
}