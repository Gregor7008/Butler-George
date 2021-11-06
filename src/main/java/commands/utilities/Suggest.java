package commands.utilities;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Suggest implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getOption("suggestion").getAsString().equals("")) {
			String channelid = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "suggest");
			if (channelid.equals(null)) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/suggest:nochannelset")).queue();
				return;
			}
			OffsetDateTime lastsuggestion = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(event.getGuild(), event.getUser(), "lastsuggestion"));
			if (Duration.between(lastsuggestion, OffsetDateTime.now()).toSeconds() < 300) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/suggest:nospam")).queue();
				return;
			}
			this.sendsuggestion(event.getGuild(), event.getMember(), event.getOption("suggestion").getAsString());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/suggest:success")).queue();
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/suggest:noargs")).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("suggest", "Suggest an idea!")
										.addOptions(new OptionData(OptionType.STRING, "suggestion", "Write down your suggestions!", true));	
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/suggest:help");
	}
	
	public void sendsuggestion(Guild guild, Member member, String idea) {
		TextChannel channel = guild.getTextChannelById(Configloader.INSTANCE.getGuildConfig(guild, "suggest"));
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
		eb.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl());
		eb.setColor(Color.YELLOW);
		eb.setFooter(OffsetDateTime.now().format(formatter));
		eb.setDescription(idea);
		Message message = channel.sendMessageEmbeds(eb.build()).complete();
		message.addReaction("U+1F44D").queue();
		message.addReaction("U+1F44E").queue();
	}
}
