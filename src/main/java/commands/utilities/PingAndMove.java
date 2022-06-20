package commands.utilities;

import java.util.concurrent.TimeUnit;

import base.Bot;
import components.base.LanguageEngine;
import components.commands.Command;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class PingAndMove implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final Member omember = guild.getMember(event.getOption("user").getAsUser());
		if (omember.equals(guild.getMember(user))) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/eastereggs:4").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (omember.equals(guild.getSelfMember())) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/eastereggs:5").convert()).queue(r -> r.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
			return;
		}
		if (!omember.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/pingandmove:memnoncon").convert()).queue();
			return;
		}
		if (!guild.getMember(user).getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/pingandmove:notcon").convert()).queue();
			return;
		}
		InteractionHook ih = event.reply(omember.getAsMention()).addEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/pingandmove:request")
				.replaceDescription("{user}", guild.getMember(user).getAsMention()).convert())
								.addActionRow(Button.primary("accept", Emoji.fromUnicode("U+2705")),
											  Button.primary("deny", Emoji.fromUnicode("U+274C"))).complete();
		Bot.run.getWaiter().waitForEvent(ButtonInteractionEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
			  	  	  return e.getUser().getIdLong() == omember.getIdLong();},
				e -> {if (e.getButton().getId().equals("accept")) {
						 guild.moveVoiceMember(guild.getMember(user), omember.getVoiceState().getChannel()).queue();
						 e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/pingandmove:accepted").convert()).queue();
						 return;}
					  if (e.getButton().getId().equals("deny")) {
						 e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/utilities/pingandmove:denied").convert()).queue();}},
				1, TimeUnit.MINUTES,
				() -> {ih.editOriginalEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/pingandmove:denied").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("pingandmove", "Join a user in a full voice channel!").addOption(OptionType.USER, "user", "The user whoms channel you want to join", true);
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
}