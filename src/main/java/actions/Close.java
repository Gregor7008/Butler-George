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

public class Close implements ActionRequest {

	//TODO Delete and implement into ModMail
	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (ConfigLoader.removeValueFromArray(ConfigLoader.getGuildConfig(guild).getJSONArray("ticketchannels"), event.getTextChannel().getIdLong())) {
			event.getTextChannel().delete().queue();
			return;
		}
		if (ConfigLoader.getModMailOfChannel(event.getTextChannel().getId()) != null) {
			String cid = event.getTextChannel().getId();
			event.getTextChannel().delete().queue();
			User cuser = ConfigLoader.getModMailOfChannel(cid);
			ConfigLoader.getModMailOfChannel(cid).openPrivateChannel().complete().sendMessageEmbeds(
					LanguageEngine.fetchMessage(guild, cuser, "/commands/moderation/close:closed").replaceDescription("{reason}", event.getOptionAsString(1)).convert()).queue();
			ConfigLoader.getFirstGuildLayerConfig(guild, "modmails").remove(String.valueOf(ConfigLoader.getModMailOfChannel(cid)));
			try {
				if (event.getOptionAsBoolean(1)) {
					ConfigLoader.getMemberConfig(guild, cuser).getJSONArray("warnings").put("Modmail abuse");
					ModEngine.run.guildPenaltyCheck(guild);
				}
			} catch (NullPointerException e) {}
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/close:nochannel"));
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("Close")
														   .setInfo("Close a ModMail ticket")
														   .setMinimumPermission(Permission.MANAGE_CHANNEL)
														   .setOptions(new OptionType[] {OptionType.STRING, OptionType.BOOLEAN})
														   .setCategory(ActionData.ADMINISTRATION);
		return actionData;
	}
}