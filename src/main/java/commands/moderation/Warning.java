package commands.moderation;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import components.moderation.PenaltyEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Warning implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		if (event.getSubcommandName().equals("add")) {
			String reason = "~Unknown reason~";
			if (event.getOption("reason") != null) {
				reason = event.getOption("reason").getAsString();
			}
			ConfigLoader.getMemberConfig(guild, iuser).getJSONArray("warnings").put(reason);
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/moderation/warning:success").convert()).queue();
			try {
				final String repl = reason;
				iuser.openPrivateChannel().queue(channel -> {
					channel.sendMessageEmbeds(AnswerEngine.fetchMessage(guild, iuser, "/commands/moderation/warning:pm")
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
				event.getChannel().sendMessageEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/warning:remsel").convert()).queue();
				EventWaiter waiter = Bot.run.getWaiter();
				TextChannel channel = event.getTextChannel();
				waiter.waitForEvent(MessageReceivedEvent.class,
						e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
							  try {Integer.parseInt(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
						  	  return e.getAuthor().getIdLong() == user.getIdLong();},
						e -> {JSONArray warnings = ConfigLoader.getMemberConfig(guild, iuser).getJSONArray("warnings");
							  int w = Integer.parseInt(e.getMessage().getContentRaw());
							  channel.sendMessageEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/warning:remsuccess")
									  .replaceDescription("{warning}", "`" + warnings.getString(w-1) + "`")
									  .replaceDescription("{user}", guild.getMember(iuser).getEffectiveName()).convert()).queue();
							  warnings.remove(w-1);},
						1, TimeUnit.MINUTES,
						() -> {channel.sendMessageEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/moderation/warning:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			}
		}
		PenaltyEngine.run.guildCheck(guild);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("warning", "0")
								  .addSubcommands(new SubcommandData("add", "Warns a user and adds a warning to their warnings-list")
											  .addOptions(new OptionData(OptionType.USER, "user", "The user you want to warn", true))
											  .addOptions(new OptionData(OptionType.STRING, "reason", "The reason why you warn the member", false)))
								  .addSubcommands(new SubcommandData("list", "Shows you the number of warnings a member already has")
										  	  .addOptions(new OptionData(OptionType.USER, "user", "The user you want to check", true)))
								  .addSubcommands(new SubcommandData("remove", "Removes a warning of a user")
										  	  .addOptions(new OptionData(OptionType.USER, "user", "The user you want to remove the warning from", true)));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getRaw(guild, user, "/commands/moderation/warning:help");
	}
	
	private boolean listwarnings(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		JSONArray warnings = ConfigLoader.getMemberConfig(guild, iuser).getJSONArray("warnings");
		if (warnings.isEmpty()) {
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/moderation/warning:nowarnings").convert()).queue();
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
		event.replyEmbeds(AnswerEngine.fetchMessage(guild, user, "/commands/moderation/warning:list")
				.replaceTitle("{user}", guild.getMemberById(iuser.getId()).getEffectiveName())
				.replaceDescription("{list}", sB.toString()).convert()).queue();
		return true;
	}
}