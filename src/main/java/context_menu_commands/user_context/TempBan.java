package context_menu_commands.user_context;

import java.time.OffsetDateTime;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import base.engines.configs.ConfigManager;
import context_menu_commands.assets.UserContextEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import slash_commands.engines.ModController;

public class TempBan implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final User target = event.getTarget();
		TextInput dayInput = TextInput.create("duration", "Duration", TextInputStyle.SHORT)
				.setMinLength(1)
				.setMaxLength(3)
				.setPlaceholder("Input duration in days")
				.build();
		Modal modal = Modal.create("configTempban", "Configure temporary ban").addActionRows(ActionRow.of(dayInput)).build();
		event.replyModal(modal).queue();
		AwaitTask.forModalInteraction(guild, user, event.getMessageChannel(),
				  d -> {
					  try {
						  int days = Integer.parseInt(d.getValue("duration").getAsString());
					        OffsetDateTime until = OffsetDateTime.now().plusDays(days);
							ConfigLoader.INSTANCE.getMemberConfig(guild, target).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
							ConfigLoader.INSTANCE.getMemberConfig(guild, target).put("tempbanned", true);
							guild.getMember(target).ban(0).queue();
							ModController.RUN.userModCheck(guild, target);
							d.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
									.replaceDescription("{user}", target.getName())
									.replaceDescription("{time}", String.valueOf(days))).setEphemeral(true).queue();
					  } catch (NumberFormatException e) {
						  d.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "error")).setEphemeral(true).queue();
					  }
				  }).append();
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "TempBan");
		context.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)).setGuildOnly(true);
		return context;
	}
}