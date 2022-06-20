package actions;

import org.json.JSONArray;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.actions.SubActionData;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class BotAutoRole implements ActionRequest {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(Action event) {
		guild = event.getGuild();
		user = event.getUser();
		if (event.getSubAction().getName().equals("add")) {
			Role role = event.getSubAction().getOptionAsRole(0);
			ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles").put(role.getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/botautorole:addsuccess"));;
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			Role role = event.getSubAction().getOptionAsRole(0);
			ConfigLoader.removeValueFromArray(ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles"), role.getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/botautorole:removesuccess"));
			return;
		}
		if (event.getSubAction().getName().equals("list")) {
			this.listroles(event);
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("BotAutoRoles")
				  											  .setInfo("Configure roles that should be given to every new bot joining")
				  											  .setMinimumPermission(Permission.MANAGE_ROLES)
				  											  .setSubActions(new SubActionData[] {
				  													  new SubActionData("add", OptionType.ROLE),
				  													  new SubActionData("remove", OptionType.ROLE),
				  													  new SubActionData("list")
				  										      })
				  											  .setCategory(ActionData.ADMINISTRATION);
		return actionData;
	}
	
	private void listroles(Action event) {
		StringBuilder sB = new StringBuilder();
		JSONArray botautoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles");
		if (botautoroles.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/botautorole:nobotautoroles"));
			return;
		}
		for (int i = 0; i < botautoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == botautoroles.length()) {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/botautorole:list").replaceDescription("{list}", sB.toString()));
	}
}