package actions.moderation;

import java.util.concurrent.TimeUnit;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class TempMute implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getOptionAsUser(0);
		this.tempmute(event.getOptionAsInt(1), guild, user);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/tempmute:success")
				.replaceDescription("{user}", user.getName())
				.replaceDescription("{time}", String.valueOf(event.getOptionAsInt(1))).convert()).queue();
	}

	@Override
	public ActionData initialize() {
		//TODO Initialize TempMute
		return null;
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.getMemberConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		ModEngine.run.guildModCheck(guild);
	}
}