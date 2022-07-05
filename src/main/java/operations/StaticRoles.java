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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class StaticRoles implements OperationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(OperationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype")).setActionRow(
				Button.primary("adminroles", Emoji.fromUnicode("\u2696")),
				Button.primary("moderationroles", Emoji.fromUnicode("\uD83D\uDC6E")),
				Button.primary("supportroles", Emoji.fromUnicode("\uD83D\uDEA8")),
				Button.primary("customchannelaccessroles", Emoji.fromUnicode("\uD83D\uDD12"))).queue();
		ResponseDetector.waitForButtonClick(guild, user, event.getMessage(), null,
				b -> {
					Toolbox.deleteActionRows(b.getMessage(), () -> {
						String type = b.getComponentId();
						if (event.getSubOperation().equals("list")) {
							this.listroles(b, type);
							return;
						}
						if (event.getSubOperation().equals("remove")) {
							ConfigLoader.getGuildConfig(guild).getJSONArray(type).clear();
							b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess").replaceDescription("{type}", type).convert()).queue();
							return;
						}
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defroles").convert()).queue();
						ResponseDetector.waitForMessage(guild, user, event.getChannel(),
								e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
								e -> {
									JSONArray ccdefaccessroles = ConfigLoader.getGuildConfig(guild).getJSONArray(type);
									List<Long> roleIDs = new ArrayList<Long>();
									e.getMessage().getMentions().getRoles().forEach(r -> roleIDs.add(r.getIdLong()));
									if (event.getSubOperation().equals("add")) {
										for (int i = 0; i < roleIDs.size(); i++) {
											if (!ccdefaccessroles.toList().contains(roleIDs.get(i))) {
												ccdefaccessroles.put(roleIDs.get(i));
											}
										}
										event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess").replaceDescription("{type}", type).convert()).queue();
									}
									if (event.getSubOperation().equals("delete")) {
										for (int i = 0; i < roleIDs.size(); i++) {
											Toolbox.removeValueFromArray(ccdefaccessroles, roleIDs.get(i));
										}
										event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess").replaceDescription("{type}", type).convert()).queue();
									}
								});
					});
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("StaticRoles")
													.setInfo("Configure the roles for different areas of responsibility")
													.setSubOperations(new SubOperationData[] {
				  											new SubOperationData("add", "Add one or more roles"),
				  											new SubOperationData("delete", "Delete one role from the active ones"),
				  											new SubOperationData("remove", "Remove all roles"),
				  											new SubOperationData("list", "List all active roles")
				  									});
		return operationData;
	}
	
	private void listroles(ButtonInteractionEvent event, String type) {
		StringBuilder sB = new StringBuilder();
		JSONArray botautoroles = ConfigLoader.getGuildConfig(guild).getJSONArray(type);
		if (botautoroles.isEmpty()) {
			event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "none").replaceDescription("{type}", type).convert()).queue();
			return;
		}
		for (int i = 0; i < botautoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == botautoroles.length()) {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list")
				.replaceDescription("{list}", sB.toString())
				.replaceDescription("{type}", type).convert()).queue();
	}
}
