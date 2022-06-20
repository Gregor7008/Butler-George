package actions.administration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

public class Goodbye implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			String message = event.getSubAction().getOptionAsString(0);
			String channelid = event.getSubAction().getOptionAsChannel(1).getId();
			ConfigLoader.getGuildConfig(guild).put("goodbyemsg", message + ";" + channelid);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/goodbye:setsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("off")) {
			ConfigLoader.getGuildConfig(guild).put("goodbyemsg", "");
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/goodbye:offsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("test")) {
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
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/goodbye:testsuccess")).queue();
			} else {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/goodbye:nonedefined")).queue();
			}
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("Goodbye")
													.setInfo("Configure a message sent on a member leaving")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", new OptionType[] {OptionType.STRING, OptionType.CHANNEL}),
															new SubActionData("off"),
															new SubActionData("off")
													});
		return actionData;
	}
}