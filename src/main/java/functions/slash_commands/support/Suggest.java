package functions.slash_commands.support;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import engines.configs.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Suggest implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		Long channelid = ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("suggest");
		if (channelid == 0) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nochannelset")).queue();
			return;
		}
		OffsetDateTime lastsuggestion = OffsetDateTime.parse(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getString("lastsuggestion"), ConfigManager.dateTimeFormatter);
		if (Duration.between(lastsuggestion, OffsetDateTime.now()).toSeconds() < 300) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nospam")).queue();
			return;
		}
		this.sendsuggestion(guild, event.getMember(), event.getOption("suggestion").getAsString());
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("suggest", "Suggest an idea!")
										.addOptions(new OptionData(OptionType.STRING, "suggestion", "Write down your suggestions!", true));
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	public void sendsuggestion(Guild guild, Member member, String idea) {
		TextChannel channel = guild.getTextChannelById(ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("suggest"));
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyyy");
		eb.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl());
		eb.setColor(Color.YELLOW);
		eb.setFooter(OffsetDateTime.now().format(formatter));
		eb.setDescription(idea);
		Message message = channel.sendMessageEmbeds(eb.build()).complete();
		message.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
		message.addReaction(Emoji.fromUnicode("U+1F44E")).queue();
	}
}