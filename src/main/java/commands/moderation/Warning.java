package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import components.moderation.PenaltyEngine;
import components.moderation.ModController;
import net.dv8tion.jda.api.entities.Guild;
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
		Guild guild = event.getGuild();
		User user = event.getUser();
		new Thread(() -> {
			new ModController().modcheck();
			PenaltyEngine.getInstance().processWarnings(guild);
		}).start();
		if (Configloader.INSTANCE.getGuildConfig(guild, "modrole").equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/warning:nomodrole")).queue();
			return;
		}
		if (!event.getMember().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "modrole")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/warning:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			final User iuser = event.getOption("user").getAsUser();
			String reason;
			if (event.getOption("reason") == null) {
				reason = "~Unknown reason~";
			} else {
				reason = event.getOption("reason").getAsString();
			}
			Configloader.INSTANCE.addUserConfig(guild, iuser, "warnings", reason);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/warning:success")).queue();
			try {
				iuser.openPrivateChannel().queue(channel -> {
					channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(
							AnswerEngine.getInstance().getTitle(guild, iuser, "/commands/moderation/warning:pm"),
							AnswerEngine.getInstance().getDescription(guild, iuser, "/commands/moderation/warning:pm").replace("{guild}", guild.getName()).replace("{reason}", reason))).queue();
				});
			} catch (Exception e) {}
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listwarnings(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			if (this.listwarnings(event)) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/warning:remsel")).queue();
				EventWaiter waiter = Bot.INSTANCE.getWaiter();
				TextChannel channel = event.getTextChannel();
				Member member = event.getOption("user").getAsMember();
				waiter.waitForEvent(GuildMessageReceivedEvent.class,
						e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
						  	  return e.getAuthor().getIdLong() == user.getIdLong();},
						e -> {String allwarnings = Configloader.INSTANCE.getUserConfig(guild, event.getOption("user").getAsUser(), "warnings");
							  String[] warnings = allwarnings.split(";");
							  int w = Integer.parseInt(e.getMessage().getContentRaw());
							  Configloader.INSTANCE.deleteUserConfig(guild, member.getUser(), "warnings", warnings[w-1]);
							  channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(
									  AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/warning:remsuccess"),
									  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/warning:remsuccess").replace("{warning}", warnings[w-1]).replace("{user}", member.getEffectiveName()))).queue();},
						1, TimeUnit.MINUTES,
						() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/warning:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("warning", "0")
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
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/warning:help");
	}
	
	private boolean listwarnings(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		final User iuser = event.getOption("user").getAsUser();
		String allwarnings = Configloader.INSTANCE.getUserConfig(guild, iuser, "warnings");
		if (allwarnings.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/warning:nowarnings")).queue();
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
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Warnings of\s" + guild.getMemberById(iuser.getId()).getEffectiveName(), sB.toString())).queue();
		return true;
	}
}