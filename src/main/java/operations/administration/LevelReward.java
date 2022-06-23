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

public class LevelReward implements OperationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(OperationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		JSONObject levelrewards = ConfigLoader.getGuildConfig(guild).getJSONObject("levelrewards");
		if (event.getSubOperation().getName().equals("add")) {
			levelrewards.put(String.valueOf(event.getSubOperation().getOptionAsInt(1)), event.getOptionAsRole(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")
					.replaceDescription("{role}", event.getOptionAsRole(0).getAsMention())
					.replaceDescription("{level}", String.valueOf(event.getSubOperation().getOptionAsInt(1)))).queue();
			return;
		}
		if (event.getSubOperation().getName().equals("remove")) {
			int level = event.getSubOperation().getOptionAsInt(0);
			try {
				levelrewards.getLong(String.valueOf(level));
			} catch (JSONException e) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noreward")).queue();
				return;
			}
			long roleID = levelrewards.getLong(String.valueOf(level));
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")
					.replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
					.replaceDescription("{level}", String.valueOf(level))).queue();
			levelrewards.remove(String.valueOf(level));
			return;
		}
		if (event.getSubOperation().getName().equals("list")) {
			this.listrewards(event, levelrewards);
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("LevelRewards")
													.setInfo("Configure rewards for leveling up")
													.setMinimumPermission(Permission.MANAGE_ROLES)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("add", new OptionType[] {OptionType.ROLE, OptionType.INTEGER}),
															new SubActionData("remove", OptionType.INTEGER),
															new SubActionData("list")
													});
		return operationData;
	}
	
	private void listrewards(OperationEvent event, JSONObject levelrewards) {
		StringBuilder sB = new StringBuilder();
		if (levelrewards.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "norewards")).queue();
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
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list").replaceDescription("{list}", sB.toString())).queue();
	}
}