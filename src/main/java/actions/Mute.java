package actions;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Mute implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getOptionAsUser(0);
		ConfigLoader.getMemberConfig(guild, user).put("muted", true);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/mute:success").replaceDescription("{user}", user.getName())).queue();
		ModEngine.run.guildModCheck(guild);
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("Mute")
													.setInfo("Mute a member permanently")
													.setMinimumPermission(Permission.MESSAGE_MANAGE)
													.setCategory(ActionData.MODERATION)
													.setOption(OptionType.USER);
		return actionData;
	}
}