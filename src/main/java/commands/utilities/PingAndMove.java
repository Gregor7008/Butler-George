package commands.utilities;

import java.util.concurrent.TimeUnit;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;

public class PingAndMove implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final Member omember = guild.getMember(event.getOption("user").getAsUser());
		if (omember.equals(guild.getMember(user))) {
			event.replyEmbeds(AnswerEngine.ae.buildMessage("You didn't...", ":man_facepalming: | You are a fucking idiot mate...")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (omember.equals(guild.getSelfMember())) {
			event.replyEmbeds(AnswerEngine.ae.buildMessage("I expected that hahahaha!",
					":rofl: | You think my developers didn't think of that?!\nThey rather wasted their brain power on this than actually fix bugs...")).queue(r -> r.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
			return;
		}
		if (!omember.getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/pingandmove:memnoncon")).queue();
			return;
		}
		if (!guild.getMember(user).getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/pingandmove:notcon")).queue();
			return;
		}
		InteractionHook ih = event.reply(omember.getAsMention()).addEmbeds(AnswerEngine.ae.buildMessage(null,
				AnswerEngine.ae.getDescription(guild, user, "/commands/utilities/pingandmove:request").replace("{user}", guild.getMember(user).getAsMention())))
								.addActionRow(Button.primary("accept", Emoji.fromUnicode("U+2705")),
											  Button.primary("deny", Emoji.fromUnicode("U+274C"))).complete();
		Bot.INSTANCE.getWaiter().waitForEvent(ButtonClickEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
			  	  	  return e.getUser().getIdLong() == omember.getIdLong();},
				e -> {if (e.getButton().getId().equals("accept")) {
						 guild.moveVoiceMember(guild.getMember(user), omember.getVoiceState().getChannel()).queue();
						 e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/pingandmove:accepted")).queue();
						 return;}
					  if (e.getButton().getId().equals("deny")) {
						 e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/pingandmove:denied")).queue();}},
				1, TimeUnit.MINUTES,
				() -> {ih.editOriginalEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/pingandmove:denied")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("pingandmove", "Join a user in a full voice channel!").addOption(OptionType.USER, "user", "The user whoms channel you want to join", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/utilities/pingandmove:help");
	}
}