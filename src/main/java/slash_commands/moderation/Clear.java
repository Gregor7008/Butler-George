package slash_commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import base.engines.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import slash_commands.assets.SlashCommandEventHandler;

public class Clear implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		TextChannel channel = guild.getTextChannelById(event.getMessageChannel().getIdLong());
		int count = Integer.parseInt(event.getOption("count").getAsString());
		List<Message> messages = channel.getHistory().retrievePast(count).complete();
//										.stream().filter(e -> {long duration =  Duration.between(e.getTimeCreated(), OffsetDateTime.now()).toDays();
//															   System.out.println(duration);
//															   return duration < 14;}).toList();
		if (messages.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "invalid")).queue();
			return;
		}
		if (messages.size() == 1) {
			messages.get(0).delete().queue();
			
		}
		try {
			channel.deleteMessages(messages).queue();
		} catch (IllegalArgumentException e) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "invalid")).queue();
			return;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "done")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("clear", "Deletes a specific number of messages from this channel!")
								      .addOptions(new OptionData(OptionType.INTEGER, "count", "Hand over the number of messages you want to delete!", true));
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
		   	   .setGuildOnly(true);
		return command;
	}
	
	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
}