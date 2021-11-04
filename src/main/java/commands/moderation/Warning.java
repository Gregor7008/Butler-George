package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import components.moderation.AutoPunishEngine;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Warning implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(Configloader.INSTANCE.getGuildConfig(event.getGuild(), "modrole")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/warning:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			final User user = event.getOption("member").getAsUser();
			Member member = event.getGuild().retrieveMemberById(user.getId()).complete();
			String reason;
			if (event.getOption("reason").getAsString().equals("")) {
				reason = "~Unknown reason~";
			} else {
				reason = event.getOption("reason").getAsString();
			}
			Configloader.INSTANCE.addUserConfig(member, "warnings", reason);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/warning:success")).queue();
			user.openPrivateChannel().queue((channel) -> {
				 channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(":warning: You have been warned :warning:", ":white_check_mark: | Reason:\n=>" + reason)).queue();});
			AutoPunishEngine.getInstance().processWarnings(event.getGuild());
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listwarnings(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			if (this.listwarnings(event)) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Choose a warning!", ":envelope_with_arrow: | Please reply with the number of the warning you want to remove!")).queue();
				EventWaiter waiter = Bot.INSTANCE.getWaiter();
				TextChannel channel = event.getTextChannel();
				User user = event.getUser();
				waiter.waitForEvent(GuildMessageReceivedEvent.class,
						e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
						  	  return e.getAuthor().getIdLong() == user.getIdLong();},
						e -> {String allwarnings = Configloader.INSTANCE.getUserConfig(event.getGuild(), event.getOption("user").getAsUser(), "warnings");
							  String[] warnings = allwarnings.split(";");
							  int w = Integer.parseInt(e.getMessage().getContentRaw());
							  Configloader.INSTANCE.deleteUserConfig(e.getMember(), "warnings", warnings[w-1]);
							  channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Success!", ":white_check_mark: | The warning for " + warnings[w-1] + " was successfully removed of the user!")).queue();},
						1, TimeUnit.MINUTES,
						() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/warning:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("warning", "Warn a member")
								  .addSubcommands(new SubcommandData("add", "Warns a user and adds a warning to their warnings-list")
											  .addOptions(new OptionData(OptionType.USER, "member", "The member you want to warn", true))
											  .addOptions(new OptionData(OptionType.STRING, "reason", "The reason why you warn the member", false)))
								  .addSubcommands(new SubcommandData("list", "Shows you the number of warnings a member already has")
										  	  .addOptions(new OptionData(OptionType.USER, "member", "The member you want to check", true)))
								  .addSubcommands(new SubcommandData("remove", "Removes the warning of a user")
										  	  .addOptions(new OptionData(OptionType.USER, "member", "The member you want to remove the warning from", true)));
		return command;
	}

	@Override
	public String getHelp() {
		return "Warn a member for rude behavior etc. The bot will keep track of it and the serveradmin can define automatic punishements when a specific number of warnings is reached!";
	}
	
	private boolean listwarnings(SlashCommandEvent event) {
		final User user = event.getOption("member").getAsUser();
		String allwarnings = Configloader.INSTANCE.getUserConfig(event.getGuild(), user, "warnings");
		if (allwarnings.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/warning:nowarnings")).queue();
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
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Warnings of\s" + event.getGuild().getMemberById(user.getId()).getEffectiveName(), sB.toString())).queue();
		return true;
	}
}
