package actions.administration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Welcome implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			String message = event.getSubAction().getOptionAsString(1);
			String channelid = event.getSubAction().getOptionAsChannel(0).getId();
			ConfigLoader.getGuildConfig(guild).put("welcomemsg", message + ";" + channelid);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/welcome:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubAction().getName().equals("off")) {
			ConfigLoader.getGuildConfig(guild).put("welcomemsg", "");
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/welcome:offsuccess").convert()).queue();
			return;
		}
		if (event.getSubAction().getName().equals("test")) {
			String welcomemsgraw = ConfigLoader.getGuildConfig(guild).getString("welcomemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (welcomemsgraw != "") {
				String[] welcomemsg = welcomemsgraw.split(";");
				String msg = welcomemsg[0].replace("{server}", guild.getName()).replace("{member}", event.getMember().getAsMention())
							.replace("{membercount}", Integer.toString(guild.getMemberCount())).replace("{date}", currentdate);
				guild.getTextChannelById(welcomemsg[1]).sendMessage(msg).queue();
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/welcome:testsuccess").convert()).queue();
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/welcome:nonedefined").convert()).queue();
			}
		}
	}

	@Override
	public ActionData initialize() {
		//TODO Initialize Welcome
		return null;
	}
}