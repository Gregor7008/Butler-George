package context;

import java.time.OffsetDateTime;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.ConfigManager;
import components.base.LanguageEngine;
import components.commands.moderation.ModController;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class TempBan implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User ruser = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, ruser, this, "defuser")).queue();
		ResponseDetector.waitForMessage(guild, ruser, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getUsers().isEmpty();},
				e -> {event.replyEmbeds(LanguageEngine.fetchMessage(guild, ruser, this, "defdays")).queue();
					  User user = e.getMessage().getMentions().getUsers().get(0);
					  ResponseDetector.waitForMessage(guild, ruser, event.getChannel(),
							  d -> {try {Integer.parseInt(d.getMessage().getContentRaw());
							  			 return true;
							  		} catch (NumberFormatException ex) {return false;}},
							  d -> {int days = Integer.parseInt(d.getMessage().getContentRaw());
							        OffsetDateTime until = OffsetDateTime.now().plusDays(days);
									ConfigLoader.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
									ConfigLoader.getMemberConfig(guild, user).put("tempbanned", true);
									guild.getMember(user).ban(0).queue();
									ModController.run.guildModCheck(guild);
									event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
											.replaceDescription("{user}", user.getName())
											.replaceDescription("{time}", String.valueOf(days))).queue();
							  });
					
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("TempBan")
															 .setInfo("Temporarily ban a user")
															 .setMinimumPermission(Permission.MESSAGE_MANAGE)
															 .setCategory(OperationData.MODERATION);
		return operationData;
	}	
}