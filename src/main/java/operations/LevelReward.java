package operations;

import org.json.JSONException;
import org.json.JSONObject;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class LevelReward implements OperationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(OperationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		JSONObject levelrewards = ConfigLoader.getGuildConfig(guild).getJSONObject("levelrewards");
		if (event.getSubOperation().equals("add")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflevel")).queue();
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					e -> {try {Integer.parseInt(e.getMessage().getContentRaw());
							   return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {int neededLevel = Integer.parseInt(e.getMessage().getContentRaw());
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defrole")).queue();
						  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
								  r -> {return !r.getMessage().getMentions().getRoles().isEmpty();},
								  r -> {long roleID = r.getMessage().getMentions().getRoles().get(0).getIdLong();
								  	    levelrewards.put(String.valueOf(neededLevel), roleID);
								  	    event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")
								  	    		.replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
								  	    		.replaceDescription("{level}", String.valueOf(neededLevel))).queue();
								  	    return;
								  });
					});
		}
		if (event.getSubOperation().equals("delete")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflevel")).queue();
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					e -> {try {Integer.parseInt(e.getMessage().getContentRaw());
							   return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {int level = Integer.parseInt(e.getMessage().getContentRaw());
						  try {
							  levelrewards.getLong(String.valueOf(level));
						  } catch (JSONException ex) {
							  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noreward")).queue();
							  return;
						  }
						  long roleID = levelrewards.getLong(String.valueOf(level));
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")
								  .replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
								  .replaceDescription("{level}", String.valueOf(level))).queue();
						  levelrewards.remove(String.valueOf(level));
						  return;
					});
		}
		if (event.getSubOperation().equals("remove")) {
			levelrewards.clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubOperation().equals("list")) {
			this.listrewards(event, levelrewards);
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("LevelRewards")
													.setInfo("Configure rewards for leveling up")
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("add", "Add a level reward"),
															new SubOperationData("delete", "Deactivate one level reward"),
															new SubOperationData("remove", "Remove all active level rewards"),
															new SubOperationData("list", "List all active level rewards")
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