package configuration_options.server;

import org.json.JSONException;
import org.json.JSONObject;

import base.assets.AwaitTask;
import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class LevelRewards implements ConfigurationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(ConfigurationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		JSONObject levelrewards = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("levelrewards");
		if (event.getSubOperation().equals("add")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflevel")).queue();
			AwaitTask.forMessageReceival(guild, user, event.getChannel(),
					e -> {try {Integer.parseInt(e.getMessage().getContentRaw());
							   return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {int neededLevel = Integer.parseInt(e.getMessage().getContentRaw());
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defrole").convert()).queue();
						  AwaitTask.forMessageReceival(guild, user, event.getChannel(),
								  r -> {return !r.getMessage().getMentions().getRoles().isEmpty();},
								  r -> {long roleID = r.getMessage().getMentions().getRoles().get(0).getIdLong();
								  	    levelrewards.put(String.valueOf(neededLevel), roleID);
								  	    event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")
								  	    		.replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
								  	    		.replaceDescription("{level}", String.valueOf(neededLevel)).convert()).queue();
								  	    return;
								  }, null).append();
					}, null).append();
		}
		if (event.getSubOperation().equals("delete")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "deflevel")).queue();
			AwaitTask.forMessageReceival(guild, user, event.getChannel(),
					e -> {try {Integer.parseInt(e.getMessage().getContentRaw());
							   return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {int level = Integer.parseInt(e.getMessage().getContentRaw());
						  try {
							  levelrewards.getLong(String.valueOf(level));
						  } catch (JSONException ex) {
							  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noreward").convert()).queue();
							  return;
						  }
						  long roleID = levelrewards.getLong(String.valueOf(level));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")
								  .replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
								  .replaceDescription("{level}", String.valueOf(level)).convert()).queue();
						  levelrewards.remove(String.valueOf(level));
						  return;
					}, null).append();
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
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("LevelRewards")
													.setInfo("Configure rewards for leveling up")
													.setSubOperations(new ConfigurationSubOptionData[] {
															new ConfigurationSubOptionData("add", "Add a level reward"),
															new ConfigurationSubOptionData("delete", "Deactivate one level reward"),
															new ConfigurationSubOptionData("remove", "Remove all active level rewards"),
															new ConfigurationSubOptionData("list", "List all active level rewards")
													});
		return configurationOptionData;
	}
	
	private void listrewards(ConfigurationEvent event, JSONObject levelrewards) {
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