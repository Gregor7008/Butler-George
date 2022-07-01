package operations;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import components.ResponseDetector;
import components.Toolbox;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class AutoRoles implements OperationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(OperationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selacc"))
						  .setActionRow(Button.primary("user", Emoji.fromFormatted(":mens:")),
								  	    Button.primary("bot", Emoji.fromFormatted(":robot:"))).queue();
		ResponseDetector.waitForButtonClick(guild, user, event.getMessage(), null,
				b -> {String selection = b.getId();
					  if (event.getSubOperation().equals("list")) {
						  this.listroles(event, selection);
						  return;
					  }
					  if (event.getSubOperation().equals("remove")) {
						  ConfigLoader.getGuildConfig(guild).getJSONArray(selection + "autoroles").clear();
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
						  return;
					  }
					  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defroles")).queue();
					  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
							  e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
							  e -> {JSONArray autoroles = ConfigLoader.getGuildConfig(guild).getJSONArray(selection + "autoroles");
							  	    List<Long> roleIDs = new ArrayList<Long>();
							  	    e.getMessage().getMentions().getRoles().forEach(r -> roleIDs.add(r.getIdLong()));
							  	    if (event.getSubOperation().equals("add")) {
							  	    	for (int i = 0; i < roleIDs.size(); i++) {
							  	    		if (!autoroles.toList().contains(roleIDs.get(i))) {
							  	    			autoroles.put(roleIDs.get(i));
							  	    		}
							  	    	}
							  	    	event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, selection + "addsuccess")).queue();
							  	    }
							  	    if (event.getSubOperation().equals("delete")) {
							  	    	for (int i = 0; i < roleIDs.size(); i++) {
							  	    		Toolbox.removeValueFromArray(autoroles, roleIDs.get(i));
							  	    	}
							  	    	event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, selection + "delsuccess")).queue();
							  	    }
							  });
					
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Auto Roles")
												    .setInfo("Configure roles that should be given to every new account joining")
				  									.setSubOperations(new SubOperationData[] {
				  											new SubOperationData("add", "Add one or more roles"),
				  											new SubOperationData("delete", "Delete one role from the active ones"),
				  											new SubOperationData("remove", "Remove all roles"),
				  											new SubOperationData("list", "List all active roles")
				  									});
		return operationData;
	}
	
	private void listroles(OperationEvent event, String selection) {
		StringBuilder sB = new StringBuilder();
		JSONArray autoroles = ConfigLoader.getGuildConfig(guild).getJSONArray(selection + "autoroles");
		if (autoroles.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, selection + "noautoroles")).queue();
			return;
		}
		for (int i = 0; i < autoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == autoroles.length()) {
				sB.append(guild.getRoleById(autoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(autoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,  this, selection + "list").replaceDescription("{list}", sB.toString())).queue();
	}
}