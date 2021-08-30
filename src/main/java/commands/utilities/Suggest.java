package commands.utilities;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Suggest implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getOption("setchannel").getAsMessageChannel().equals(null)) {
			if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/suggest:nopermission")).queue();
			} else {
				Configloader.INSTANCE.setGuildConfig(event.getGuild(), "suggest", event.getOption("setchannel").getAsGuildChannel().getId());
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/suggest:successset")).queue();
			}
			return;
		}
		if (!event.getOption("idea").getAsString().equals("")) {
			TextChannel channel = event.getGuild().getTextChannelById(Configloader.INSTANCE.getGuildConfig(event.getGuild(), "suggest"));
			EmbedBuilder eb = new EmbedBuilder();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
			eb.setColor(Color.YELLOW);
			eb.setFooter("Suggestion on " + OffsetDateTime.now().format(formatter));
			eb.setDescription(event.getOption("suggestion").getAsString());
			Message message = channel.sendMessageEmbeds(eb.build()).complete();
			message.addReaction(":thumbsup:").queue();
			message.addReaction(":thumbsdown:").queue();
			return;
		}
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/suggest:noargs")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("suggest", "Suggest an idea!")
										.addOptions(new OptionData(OptionType.CHANNEL, "setchannel", "Set the channel for suggestions!").setRequired(false))
										.addOptions(new OptionData(OptionType.STRING, "idea", "Write down your suggestions!").setRequired(false));	
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to suggest a new idea to the server team!";
	}
}
