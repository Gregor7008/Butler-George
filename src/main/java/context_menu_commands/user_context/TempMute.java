package context_menu_commands.user_context;

import java.util.concurrent.TimeUnit;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import context_menu_commands.assets.UserContextEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import slash_commands.engines.ModController;

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
		CommandData context = Commands.context(Command.Type.USER, "TempMute").setGuildOnly(true);
		return context;
	}

	@Override
	public boolean checkBotPermissions(UserContextInteractionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailableTo(Member member) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		ModController.RUN.userModCheck(guild, user);
	}
}