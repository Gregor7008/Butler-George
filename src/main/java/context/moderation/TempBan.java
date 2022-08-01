package context.moderation;

import java.time.OffsetDateTime;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.ConfigManager;
import components.base.LanguageEngine;
import components.commands.ModController;
import components.context.UserContextEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class TempBan implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final User target = event.getTarget();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defdays").convert()).queue();
		ResponseDetector.waitForMessage(guild, user, event.getMessageChannel(),
				  d -> {try {Integer.parseInt(d.getMessage().getContentRaw());
				  			return true;
				  		} catch (NumberFormatException ex) {return false;}},
				  d -> {int days = Integer.parseInt(d.getMessage().getContentRaw());
				        OffsetDateTime until = OffsetDateTime.now().plusDays(days);
						ConfigLoader.getMemberConfig(guild, target).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
						ConfigLoader.getMemberConfig(guild, target).put("tempbanned", true);
						guild.getMember(target).ban(0).queue();
						ModController.run.userModCheck(guild, target);
						event.getMessageChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
								.replaceDescription("{user}", target.getName())
								.replaceDescription("{time}", String.valueOf(days)).convert()).queue();
				  });
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "TempBan");
		return context;
	}	
}