package actions.administration;

import org.json.JSONException;
import org.json.JSONObject;

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

public class Join2Create implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final String id = event.getSubAction().getOptionAsChannel(0).getId();
		JSONObject join2createchannels = ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels");
		if (guild.getVoiceChannelById(id) == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/join2create:invalid")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			try {
				join2createchannels.get(id);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/join2create:adderror")).queue();
			} catch (JSONException e) {
				join2createchannels.put(id, new JSONObject());
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/join2create:addsuccess")).queue();
			}
		}
		if (event.getSubAction().getName().equals("remove")) {
			try {
				join2createchannels.get(id);
				join2createchannels.remove(id);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/join2create:remsuccess")).queue();
			} catch (JSONException e) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/join2create:remerror")).queue();
			}
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("Join2Create")
													.setInfo("Configure Join2Create channels for your server")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("add", OptionType.CHANNEL),
															new SubActionData("remove", OptionType.CHANNEL)
													});
		return actionData;
	}
}