package context;

import java.util.concurrent.TimeUnit;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModController;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class TempMute implements OperationEventHandler {

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
							  	    this.tempmute(days, guild, user);
							  	    event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
										 .replaceDescription("{user}", user.getName())
										 .replaceDescription("{time}", String.valueOf(days)).convert()).queue();
							  });
					
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("TempMute")
															 .setInfo("Mute a member temporarily")
															 .setMinimumPermission(Permission.MESSAGE_MANAGE)
															 .setCategory(OperationData.MODERATION);
		return operationData;
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.getMemberConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		ModController.run.guildModCheck(guild);
	}
}