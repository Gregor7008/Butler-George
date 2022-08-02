package context_menu_commands.moderation;

import java.util.concurrent.TimeUnit;

import base.assets.AwaitTask;
import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import context_menu_commands.assets.UserContextEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import slash_commands.engines.ModController;

public class TempMute implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final User target = event.getTarget();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defdays")).queue();
		AwaitTask.forMessageReceival(guild, user, event.getMessageChannel(),
				d -> {try {Integer.parseInt(d.getMessage().getContentRaw());
						 return true;
				      } catch (NumberFormatException ex) {return false;}},
				d -> {int days = Integer.parseInt(d.getMessage().getContentRaw());
					  this.tempmute(days, guild, target);
					  event.getMessageChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
							 .replaceDescription("{user}", target.getAsMention())
							 .replaceDescription("{time}", String.valueOf(days))).queue();
				}, null).append();
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "TempMute");
		return context;
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		ModController.RUN.userModCheck(guild, user);
	}
}