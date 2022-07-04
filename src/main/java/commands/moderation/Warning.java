package commands.moderation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import components.commands.ModController;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Warning implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		if (event.getSubcommandName().equals("add")) {
			String reason = "~Unknown reason~";
			if (event.getOption("reason") != null) {
				reason = event.getOption("reason").getAsString();
			}
			ConfigLoader.getMemberConfig(guild, iuser).getJSONArray("warnings").put(reason);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
			try {
				final String repl = reason;
				iuser.openPrivateChannel().queue(channel -> {
					channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, iuser, this, "pm")
							.replaceDescription("{guild}", guild.getName())
							.replaceDescription("{reason}", repl).convert()).queue();
				});
			} catch (Exception e) {}
		}
		if (event.getSubcommandName().equals("list")) {
			this.listwarnings(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			if (this.listwarnings(event)) {
				event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsel").convert()).queue();
				TextChannel channel = event.getTextChannel();
				ResponseDetector.waitForMessage(guild, user, channel,
						e -> {try {
								  Integer.parseInt(e.getMessage().getContentRaw());
								  return true;
							  } catch (NumberFormatException ex) {return false;}},
						e -> {JSONArray warnings = ConfigLoader.getMemberConfig(guild, iuser).getJSONArray("warnings");
							  int w = Integer.parseInt(e.getMessage().getContentRaw());
							  channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")
									  .replaceDescription("{warning}", "`" + warnings.getString(w-1) + "`")
									  .replaceDescription("{user}", guild.getMember(iuser).getEffectiveName()).convert()).queue();
							  warnings.remove(w-1);});
			}
		}
		ModController.run.guildPenaltyCheck(guild);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("warning", "0")
								  .addSubcommands(new SubcommandData("add", "Warns a user and adds a warning to their warnings-list")
											  			.addOptions(new OptionData(OptionType.USER, "user", "The user you want to warn", true))
											  			.addOptions(new OptionData(OptionType.STRING, "reason", "The reason why you warn the member", false)),
											  	  new SubcommandData("list", "Shows you the number of warnings a member already has")
											  	  		.addOptions(new OptionData(OptionType.USER, "user", "The user you want to check", true)),
											  	  new SubcommandData("remove", "Removes a warning of a user")
											  	  		.addOptions(new OptionData(OptionType.USER, "user", "The user you want to remove the warning from", true)));
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
		   	   .setGuildOnly(true);
		return command;
	}
	
	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		List<Role> roles = new ArrayList<>();
		Role moderationrole = guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("moderationrole"));
		roles.add(moderationrole);
		return roles;
	}
	
	private boolean listwarnings(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		JSONArray warnings = ConfigLoader.getMemberConfig(guild, iuser).getJSONArray("warnings");
		if (warnings.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nowarnings").convert()).queue();
			return false;
		}
		StringBuilder sB = new StringBuilder();
		sB.append("Warning-Count:\s" + warnings.length() + "\n");
		for (int i = 0; i < warnings.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i+1) + "\s")
			  .append(warnings.get(i));
			if (i+1 != warnings.length()) {
				sB.append("\n");
			} else {}
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list")
				.replaceTitle("{user}", guild.getMemberById(iuser.getId()).getEffectiveName())
				.replaceDescription("{list}", sB.toString()).convert()).queue();
		return true;
	}
}