package functions.context_menu_commands.user_context;

import org.json.JSONObject;

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

public class Unmute implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final User target = event.getTarget();
		JSONObject userconfig = ConfigLoader.INSTANCE.getMemberConfig(guild, target);
		if (!guild.getMember(target).isTimedOut()) {
			if (!userconfig.getBoolean("muted") && !userconfig.getBoolean("tempmuted")) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nomute").replaceDescription("{user}", target.getAsMention())).setEphemeral(true).queue();
				return;
			}
		}
		userconfig.put("muted", false);
		userconfig.put("tempmuted", false);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
				.replaceDescription("{user}", target.getAsMention())).setEphemeral(true).queue();
		ModController.RUN.userModCheck(guild, target);
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "Unmute");
		context.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)).setGuildOnly(true);
		return context;
	}
}