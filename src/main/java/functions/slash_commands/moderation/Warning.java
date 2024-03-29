package functions.slash_commands.moderation;

import org.json.JSONArray;

import assets.base.AwaitTask;
import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import engines.functions.ModController;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Warning implements SlashCommandEventHandler {

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
			ConfigLoader.INSTANCE.getMemberConfig(guild, iuser).getJSONArray("warnings").put(reason);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")).queue();
			try {
				final String repl = reason;
				iuser.openPrivateChannel().queue(channel -> {
					channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, iuser, this, "pm")
							.replaceDescription("{guild}", guild.getName())
							.replaceDescription("{reason}", repl)).queue();
				});
			} catch (Exception e) {}
		}
		if (event.getSubcommandName().equals("list")) {
			this.listwarnings(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			if (this.listwarnings(event)) {
				event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsel")).queue();
				TextChannel channel = guild.getTextChannelById(event.getMessageChannel().getIdLong());
				AwaitTask.forMessageReceival(guild, user, channel,
						e -> {try {
								  Integer.parseInt(e.getMessage().getContentRaw());
								  return true;
							  } catch (NumberFormatException ex) {return false;}},
						e -> {JSONArray warnings = ConfigLoader.INSTANCE.getMemberConfig(guild, iuser).getJSONArray("warnings");
							  int w = Integer.parseInt(e.getMessage().getContentRaw());
							  channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")
									  .replaceDescription("{warning}", "`" + warnings.getString(w-1) + "`")
									  .replaceDescription("{user}", guild.getMember(iuser).getEffectiveName())).queue();
							  warnings.remove(w-1);}, null).append();
			}
		}
		ModController.RUN.guildPenaltyCheck(guild);
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
	
	private boolean listwarnings(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		JSONArray warnings = ConfigLoader.INSTANCE.getMemberConfig(guild, iuser).getJSONArray("warnings");
		if (warnings.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nowarnings")).queue();
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
				.replaceDescription("{list}", sB.toString())).queue();
		return true;
	}
}