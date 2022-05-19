package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
		if (event.getSubcommandName().equals("add")) {
			final User iuser = event.getOption("user").getAsUser();
			String reason;
			if (event.getOption("reason") == null) {
				reason = "~Unknown reason~";
			} else {
				reason = event.getOption("reason").getAsString();
			}
			ConfigLoader.cfl.addUserConfig(guild, iuser, "warnings", reason);
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/warning:success").convert()).queue();
			try {
				iuser.openPrivateChannel().queue(channel -> {
					channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, iuser, "/commands/moderation/warning:pm")
							.replaceDescription("{guild}", guild.getName())
							.replaceDescription("{reason}", reason).convert()).queue();
				});
			} catch (Exception e) {}
		}
		if (event.getSubcommandName().equals("list")) {
			this.listwarnings(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			if (this.listwarnings(event)) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/warning:remsel").convert()).queue();
				EventWaiter waiter = Bot.INSTANCE.getWaiter();
				TextChannel channel = event.getTextChannel();
				Member member = event.getOption("user").getAsMember();
				waiter.waitForEvent(MessageReceivedEvent.class,
						e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
						  	  return e.getAuthor().getIdLong() == user.getIdLong();},
						e -> {String allwarnings = ConfigLoader.cfl.getUserConfig(guild, event.getOption("user").getAsUser(), "warnings");
							  String[] warnings = allwarnings.split(";");
							  int w = Integer.parseInt(e.getMessage().getContentRaw());
							  ConfigLoader.cfl.removeUserConfig(guild, member.getUser(), "warnings", warnings[w-1]);
							  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/warning:remsuccess")
									  .replaceDescription("{warning}", warnings[w-1])
									  .replaceDescription("{user}", member.getEffectiveName()).convert()).queue();},
						1, TimeUnit.MINUTES,
						() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/warning:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			}
		}
		Bot.INSTANCE.penaltyCheck(guild);
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
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/warning:help");
	}
	
	private boolean listwarnings(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		String allwarnings = ConfigLoader.cfl.getUserConfig(guild, iuser, "warnings");
		if (allwarnings.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/warning:nowarnings").convert()).queue();
			return false;
		}
		String[] warnings = allwarnings.split(";");
		StringBuilder sB = new StringBuilder();
		sB.append("Warning-Count:\s" + warnings.length + "\n");
		for (int i = 0; i < warnings.length; i++) {
			sB.append('#')
			  .append(String.valueOf(i+1) + "\s")
			  .append(warnings[i]);
			if (i+1 != warnings.length) {
				sB.append("\n");
			} else {}
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/warning:list")
				.replaceTitle("{user}", guild.getMemberById(iuser.getId()).getEffectiveName())
				.replaceDescription("{list}", sB.toString()).convert()).queue();
		return true;
	}
}