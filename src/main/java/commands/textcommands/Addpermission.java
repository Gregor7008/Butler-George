package commands.textcommands;

import java.util.concurrent.TimeUnit;

import commands.TextCommand;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Addpermission implements TextCommand{

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		final User user = event.getAuthor();
		final Guild guild = event.getGuild();
		final TextChannel channel = event.getChannel();
		if (!channel.getParent().equals(guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory")))) {
			event.getMessage().delete().queue();
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/textcommands/addpermission:nopermission")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (argument.equals("")) {
			event.getMessage().delete();
		}
	}
}