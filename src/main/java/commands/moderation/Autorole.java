package commands.moderation;

import base.Configloader;
import commands.Commands;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Autorole implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		if (argument.startsWith("add")) {
			final Role role;
			try {
				role = event.getMessage().getMentionedRoles().get(0);
			} catch (Exception e) {
				AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:needrole", event).queue();
				return;
			}
			Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autoroles", role.getId());
		}
		if (argument.startsWith("remove")) {
			final Role role;
			try {
				role = event.getMessage().getMentionedRoles().get(0);
			} catch (Exception e) {
				AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:needrole", event).queue();
				return;
			}
			Configloader.INSTANCE.deleteGuildConfig(event.getGuild(), "autoroles", role.getId());
		}
		if (argument.startsWith("list")) {
			final StringBuilder sB = new StringBuilder();
			final String currentraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "autoroles");
			if (currentraw == "") {
				AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:noautoroles", event);
				return;
			}
			if (!currentraw.contains(";")) {
				AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new member joins:", event.getGuild().getRoleById(currentraw).getAsMention(), event.getChannel()).queue();
				return;
			}
			String[] current = currentraw.split(";");
			for (int i = 1; i <= current.length; i++) {
				sB.append('#')
				  .append(String.valueOf(i) + " ")
				  .append(event.getGuild().getRoleById(current[i-1]).getAsMention());
			}
			AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new member joins:", sB.toString(), event.getChannel()).queue();
		}
	}

}
