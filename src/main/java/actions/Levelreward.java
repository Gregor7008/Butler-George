package actions;

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

public class Levelreward implements ActionRequest {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(Action event) {
		guild = event.getGuild();
		user = event.getUser();
		JSONObject levelrewards = ConfigLoader.getGuildConfig(guild).getJSONObject("levelrewards");
		if (event.getSubAction().getName().equals("add")) {
			levelrewards.put(String.valueOf(event.getSubAction().getOptionAsInt(1)), event.getOptionAsRole(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/levelreward:addsuccess")
					.replaceDescription("{role}", event.getOptionAsRole(0).getAsMention())
					.replaceDescription("{level}", String.valueOf(event.getSubAction().getOptionAsInt(1)))).queue();
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			int level = event.getSubAction().getOptionAsInt(0);
			try {
				levelrewards.getLong(String.valueOf(level));
			} catch (JSONException e) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/levelreward:noreward")).queue();
				return;
			}
			long roleID = levelrewards.getLong(String.valueOf(level));
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/levelreward:remsuccess")
					.replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
					.replaceDescription("{level}", String.valueOf(level))).queue();
			levelrewards.remove(String.valueOf(level));
			return;
		}
		if (event.getSubAction().getName().equals("list")) {
			this.listrewards(event, levelrewards);
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("LevelRewards")
													.setInfo("Configure rewards for leveling up")
													.setMinimumPermission(Permission.MANAGE_ROLES)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("add", new OptionType[] {OptionType.ROLE, OptionType.INTEGER}),
															new SubActionData("remove", OptionType.INTEGER),
															new SubActionData("list")
													});
		return actionData;
	}
	
	private void listrewards(Action event, JSONObject levelrewards) {
		StringBuilder sB = new StringBuilder();
		if (levelrewards.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/levelreward:norewards")).queue();
			return;
		}
		Object[] rewards = levelrewards.keySet().toArray();
		for (int i = 0; i < rewards.length; i++) {
			sB.append('#')
			  .append(String.valueOf(i+1) + "\s\s");
			if (i+1 == rewards.length) {
				sB.append(guild.getRoleById(levelrewards.getLong((String) rewards[i])).getAsMention() + "\s->\s" + rewards[i]);
			} else {
				sB.append(guild.getRoleById(levelrewards.getLong((String) rewards[i])).getAsMention() + "\s->\s" + rewards[i] + "\n");
			}
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/levelreward:list").replaceDescription("{list}", sB.toString())).queue();
	}
}