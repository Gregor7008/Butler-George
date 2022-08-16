package slash_commands.utilities;

import java.util.concurrent.TimeUnit;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import slash_commands.assets.SlashCommandEventHandler;

public class PingAndMove implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final Member omember = guild.getMember(event.getOption("user").getAsUser());
		if (omember.equals(guild.getMember(user))) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "4")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (omember.equals(guild.getSelfMember())) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "5")).queue(r -> r.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
			return;
		}
		if (!omember.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "memnoncon")).queue();
			return;
		}
		if (!guild.getMember(user).getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "notcon")).queue();
			return;
		}
		InteractionHook ih = event.reply(omember.getAsMention()).addEmbeds(LanguageEngine.fetchMessage(guild, user, this, "request")
				.replaceDescription("{user}", guild.getMember(user).getAsMention()))
								.addActionRow(Button.secondary("accept", Emoji.fromUnicode("U+2705")),
											  Button.secondary("deny", Emoji.fromUnicode("U+274C"))).complete();
		AwaitTask.forButtonInteraction(guild, user, ih.retrieveOriginal().complete(), null,
				e -> {if (e.getButton().getId().equals("accept")) {
						 guild.moveVoiceMember(guild.getMember(user), omember.getVoiceState().getChannel()).queue();
						 e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "accepted")).queue();
						 return;}
					  if (e.getButton().getId().equals("deny")) {
						 e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "denied")).queue();}},
				() -> {ih.editOriginalEmbeds(LanguageEngine.fetchMessage(guild, user, this, "denied")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));}).append();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("pingandmove", "Join a user in a full voice channel!")
									  .addOption(OptionType.USER, "user", "The user whoms channel you want to join", true);
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}

	@Override
	public boolean checkBotPermissions(SlashCommandInteractionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailableTo(Member member) {
		// TODO Auto-generated method stub
		return false;
	}
}