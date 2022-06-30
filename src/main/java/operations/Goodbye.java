package operations;

import java.time.OffsetDateTime;

import org.json.JSONArray;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Goodbye implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		JSONArray goodbyemsg = ConfigLoader.getGuildConfig(guild).getJSONArray("goodbyemsg");
		boolean defined = goodbyemsg.getString(0).equals("");
		if (event.getSubOperation().equals("set")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setrequest1")).queue();
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					e -> {return !e.getMessage().getMentions().getChannels().isEmpty();},
					e -> {goodbyemsg.put(1, e.getMessage().getMentions().getChannels().get(0).getIdLong());
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setrequest2")).queue();
						  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
								  a -> {goodbyemsg.put(0, a.getMessage().getContentRaw());
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
			if (!goodbyemsg.getBoolean(2)) {
				goodbyemsg.put(2, true);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onsuccess")).queue();
				return;
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "onfail")).queue();
				return;
			}
		}
		if (event.getSubOperation().equals("off")) {
			if (goodbyemsg.getBoolean(2)) {
				goodbyemsg.put(2, false);
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess")).queue();
				return;
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offfail")).queue();
				return;
			}
		}
		if (event.getSubOperation().equals("test")) {
			String msg = goodbyemsg.getString(1);
			msg.replace("{server}", guild.getName());
			msg.replace("{user}", event.getMember().getAsMention());
			msg.replace("{membercount}", Integer.toString(guild.getMemberCount()));
			msg.replace("{date}", OffsetDateTime.now().format(LanguageEngine.formatter));
			guild.getTextChannelById(goodbyemsg.getLong(1)).sendMessage(msg).queue();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess")).queue();
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Goodbye")
													.setInfo("Configure a message sent on a member leaving")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("set", "Set a goodbye message for leaving members"),
															new SubOperationData("on", "Activate (and set if not already done) the goodbye message"),
															new SubOperationData("off", "Deactivate the goodbye message"),
															new SubOperationData("test", "Test the currently set goodbye message")
													});
		return operationData;
	}
}