package operations.administration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operation.OperationEvent;
import components.operation.OperationRequest;
import components.operation.OperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Welcome implements OperationRequest {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			String message = event.getSubAction().getOptionAsString(1);
			String channelid = event.getSubAction().getOptionAsChannel(0).getId();
			ConfigLoader.getGuildConfig(guild).put("welcomemsg", message + ";" + channelid);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess").convert()).queue();
			return;
		}
		if (event.getSubAction().getName().equals("off")) {
			ConfigLoader.getGuildConfig(guild).put("welcomemsg", "");
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess").convert()).queue();
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
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess").convert()).queue();
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined").convert()).queue();
			}
		}
	}

	@Override
	public OperationData initialize() {
		//TODO Initialize Welcome
		return null;
	}
}