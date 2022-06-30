package operations.administration;

import java.time.OffsetDateTime;

import org.json.JSONArray;
import org.json.JSONException;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import components.utilities.ResponseDetector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Welcome implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		JSONArray welcomemsg = ConfigLoader.getGuildConfig(guild).getJSONArray("welcomemsg");
		boolean defined = false;
		try {
			defined = welcomemsg.getString(0).equals("");
		} catch (JSONException e) {}
		if (event.getSubOperation().equals("set")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setrequest1")).queue();
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					e -> {return !e.getMessage().getMentions().getChannels().isEmpty();},
					e -> {welcomemsg.put(1, e.getMessage().getMentions().getChannels().get(0).getIdLong());
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setrequest2")).queue();
						  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
								  a -> {welcomemsg.put(0, a.getMessage().getContentRaw());
									  	event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
									  	return;
								  });
					});
		}
		if (!defined) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined")).queue();
			return;
		}
		if (event.getSubOperation().equals("on")) {
			if (!welcomemsg.getBoolean(2)) {
				welcomemsg.put(2, true);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onsuccess")).queue();
				return;
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onfail")).queue();
				return;
			}
		}
		if (event.getSubOperation().equals("off")) {
			if (welcomemsg.getBoolean(2)) {
				welcomemsg.put(2, false);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess")).queue();
				return;
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offfail")).queue();
				return;
			}
		}
		if (event.getSubOperation().equals("test")) {
			String msg = welcomemsg.getString(1);
			msg.replace("{server}", guild.getName());
			msg.replace("{member}", event.getMember().getAsMention());
			msg.replace("{membercount}", Integer.toString(guild.getMemberCount()));
			msg.replace("{date}", OffsetDateTime.now().format(LanguageEngine.formatter));
			guild.getTextChannelById(welcomemsg.getLong(1)).sendMessage(msg).queue();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess")).queue();
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Welcome")
													.setInfo("Configure a message sent on a member joining")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("set", "Set a welcome message for new members"),
															new SubOperationData("on", "Activate (and set if not already done) the welcome message"),
															new SubOperationData("off", "Deactivate the welcome message"),
															new SubOperationData("test", "Test the currently set welcome message")
													});
		return operationData;
	}
}