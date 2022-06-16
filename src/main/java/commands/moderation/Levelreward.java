package commands.moderation;

import org.json.JSONException;
import org.json.JSONObject;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Levelreward implements Command{

	private Guild guild;
	private User user;
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		JSONObject levelrewards = ConfigLoader.getGuildConfig(guild).getJSONObject("levelrewards");
		if (event.getSubcommandName().equals("add")) {
			levelrewards.put(String.valueOf(event.getOption("level").getAsInt()), event.getOption("role").getAsRole().getIdLong());
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/levelreward:addsuccess")
					.replaceDescription("{role}", event.getOption("role").getAsRole().getAsMention())
					.replaceDescription("{level}", String.valueOf(event.getOption("level").getAsInt())).convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			int level = event.getOption("level").getAsInt();
			try {
				levelrewards.getLong(String.valueOf(level));
			} catch (JSONException e) {
				event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/moderation/levelreward:noreward").convert()).queue();
				return;
			}
			long roleID = levelrewards.getLong(String.valueOf(level));
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/levelreward:remsuccess")
					.replaceDescription("{role}", guild.getRoleById(roleID).getAsMention())
					.replaceDescription("{level}", String.valueOf(level)).convert()).queue();
			levelrewards.remove(String.valueOf(level));
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listrewards(event, levelrewards);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("levelreward", "0")
									.addSubcommands(new SubcommandData("add", "Adds a new levelreward in form of a role")
											.addOption(OptionType.ROLE, "role", "The roles that should be given", true)
											.addOption(OptionType.INTEGER, "level", "The level on which the role should be given to a user", true))
									.addSubcommands(new SubcommandData("remove", "Removes an existing levelreward").addOption(OptionType.INTEGER, "level", "The level of which the reward should be removed", true))
									.addSubcommands(new SubcommandData("list", "Lists all existing levelrewards"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getRaw(guild, user, "/commands/moderation/levelreward:help");
	}
	
	private void listrewards(SlashCommandInteractionEvent event, JSONObject levelrewards) {
		StringBuilder sB = new StringBuilder();
		if (levelrewards.isEmpty()) {
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/moderation/levelreward:norewards").convert()).queue();;
			return;
		}
		String[] rewards = (String[]) levelrewards.keySet().toArray();
		for (int i = 0; i < rewards.length; i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == rewards.length) {
				sB.append(guild.getRoleById(levelrewards.getLong(rewards[i])).getAsMention() + "\s->\s" + rewards[i]);
			} else {
				sB.append(guild.getRoleById(levelrewards.getLong(rewards[i])).getAsMention() + "\s->\s" + rewards[i] + "\n");
			}
		}
		event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/levelreward:list").replaceDescription("{list}", sB.toString()).convert()).queue();
	}
}