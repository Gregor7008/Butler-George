package commands.moderation;

import base.Configloader;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Autorole{

	
	public Autorole(Guild guild, Member member, TextChannel channel, Role role, String argument) {
		if (argument.startsWith("add")) {
			if (role == null) {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:needrole", guild, member)).queue();
				return;
			}
			Configloader.INSTANCE.addGuildConfig(guild, "autoroles", role.getId());
		}
		if (argument.startsWith("remove")) {
			if (role == null) {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:needrole", guild, member)).queue();
				return;
			}
			Configloader.INSTANCE.deleteGuildConfig(guild, "autoroles", role.getId());
		}
		if (argument.startsWith("list")) {
			final StringBuilder sB = new StringBuilder();
			final String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "autoroles");
			if (currentraw == "") {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:noautoroles", guild, member));
				return;
			}
			if (!currentraw.contains(";")) {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new member joins:", guild.getRoleById(currentraw).getAsMention())).queue();
				return;
			}
			String[] current = currentraw.split(";");
			for (int i = 1; i <= current.length; i++) {
				sB.append('#')
				  .append(String.valueOf(i) + " ")
				  .append(guild.getRoleById(current[i-1]).getAsMention());
			}
			channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new member joins:", sB.toString())).queue();
		}
	}

}
