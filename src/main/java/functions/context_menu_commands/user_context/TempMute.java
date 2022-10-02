package functions.context_menu_commands.user_context;

import java.util.concurrent.TimeUnit;

import assets.base.AwaitTask;
import assets.functions.UserContextEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import engines.functions.ModController;
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

public class TempMute implements UserContextEventHandler {

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
						this.tempmute(days, guild, target);
						event.getMessageChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
								.replaceDescription("{user}", target.getAsMention())
								.replaceDescription("{time}", String.valueOf(days))).queue();
					} catch (NumberFormatException e) {
						d.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "error")).setEphemeral(true).queue();
					}
				}).append();
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "TempMute");
		context.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)).setGuildOnly(true);
		return context;
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		ModController.RUN.userModCheck(guild, user);
	}
}