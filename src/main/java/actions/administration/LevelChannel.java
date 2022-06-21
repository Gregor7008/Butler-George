package actions.administration;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.actions.SubActionData;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class LevelChannel implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("communityinbox", event.getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("communityinbox", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess")).queue();
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("LevelChannel")
													.setInfo("Configure a channel for level-up messsages")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.CHANNEL),
															new SubActionData("clear", OptionType.CHANNEL)
													});
		return actionData;
	}
}