package operations.administration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

public class Goodbye implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubOperation().getName().equals("set")) {
			String message = event.getSubOperation().getOptionAsString(0);
			String channelid = event.getSubOperation().getOptionAsChannel(1).getId();
			ConfigLoader.getGuildConfig(guild).put("goodbyemsg", message + ";" + channelid);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
			return;
		}
		if (event.getSubOperation().getName().equals("off")) {
			ConfigLoader.getGuildConfig(guild).put("goodbyemsg", "");
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "offsuccess")).queue();
			return;
		}
		if (event.getSubOperation().getName().equals("test")) {
			String goodbyemsgraw = ConfigLoader.getGuildConfig(guild).getString("goodbyemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (goodbyemsgraw != "") {
				String[] goodbyemsg = goodbyemsgraw.split(";");
				String msg = goodbyemsg[0].replace("{server}", guild.getName()).replace("{member}", event.getMember().getEffectiveName())
							.replace("{membercount}", Integer.toString(guild.getMemberCount()))
							.replace("{timejoined}", event.getMember().getTimeJoined().format(formatter)).replace("{date}", currentdate);
				guild.getTextChannelById(goodbyemsg[1]).sendMessage(msg).queue();
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "testsuccess")).queue();
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nonedefined")).queue();
			}
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Goodbye")
													.setInfo("Configure a message sent on a member leaving")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", new OptionType[] {OptionType.STRING, OptionType.CHANNEL}),
															new SubActionData("off"),
															new SubActionData("off")
													});
		return operationData;
	}
}