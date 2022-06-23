package operations.administration;

import org.json.JSONException;
import org.json.JSONObject;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubActionData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Join2Create implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final String id = event.getSubOperation().getOptionAsChannel(0).getId();
		JSONObject join2createchannels = ConfigLoader.getGuildConfig(guild).getJSONObject("join2createchannels");
		if (guild.getVoiceChannelById(id) == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "invalid")).queue();
			return;
		}
		if (event.getSubOperation().getName().equals("add")) {
			try {
				join2createchannels.get(id);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "adderror")).queue();
			} catch (JSONException e) {
				join2createchannels.put(id, new JSONObject());
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")).queue();
			}
		}
		if (event.getSubOperation().getName().equals("remove")) {
			try {
				join2createchannels.get(id);
				join2createchannels.remove(id);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			} catch (JSONException e) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remerror")).queue();
			}
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Join2Create")
													.setInfo("Configure Join2Create channels for your server")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("add", OptionType.CHANNEL),
															new SubActionData("remove", OptionType.CHANNEL)
													});
		return operationData;
	}
}