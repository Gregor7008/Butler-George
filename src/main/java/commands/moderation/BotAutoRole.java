package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class BotAutoRole implements Command{

	private Guild guild;
	private User user;
	
	@Override
	public void perform(SlashCommandEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/botautorole:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			Role role = event.getOption("addrole").getAsRole();
			Configloader.INSTANCE.addGuildConfig(guild, "botautoroles", role.getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/botautorole:addsuccess")).queue();;
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			Role role = event.getOption("removerole").getAsRole();
			Configloader.INSTANCE.deleteGuildConfig(guild, "botautoroles", role.getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/botautorole:removesuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listroles(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("botautorole", "0")
								  .addSubcommands(new SubcommandData("add", "Adds a role, that'll be given to every new member joining!")
										  .addOptions(new OptionData(OptionType.ROLE, "addrole", "Mention the role you want to add!", true)))
								  .addSubcommands(new SubcommandData("remove", "Removes a role, that was previously given to every new member joining!")
										  .addOptions(new OptionData(OptionType.ROLE, "removerole", "Mention the role you want to remove!", true)))
								  .addSubcommands(new SubcommandData("list", "Displays the roles currently given to every new member joining!"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/botautorole:help");
	}
	
	private void listroles(SlashCommandEvent event) {
		StringBuilder sB = new StringBuilder();
		String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "botautoroles");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/botautorole:nobotautoroles")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/botautorole:list"), "#1\s\s" + guild.getRoleById(currentraw).getAsMention())).queue();
			return;
		}
		String[] current = currentraw.split(";");
		for (int i = 1; i <= current.length; i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i == current.length) {
				sB.append(guild.getRoleById(current[i-1]).getAsMention());
			} else {
				sB.append(guild.getRoleById(current[i-1]).getAsMention() + "\n");
			}
		}
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/botautorole:list"), sB.toString())).queue();
	}
}