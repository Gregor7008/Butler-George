package commands.moderation;

import commands.Command;
import components.Developerlist;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Botautorole implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		if (!member.hasPermission(Permission.MANAGE_ROLES) && !Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/botautorole:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			final Role role = event.getOption("addrole").getAsRole();
			Configloader.INSTANCE.addGuildConfig(guild, "botautoroles", role.getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/botautorole:addsuccess")).queue();;
			return;
		}
		if (event.getSubcommandName().equals("remove")) {
			final Role role = event.getOption("removerole").getAsRole();
			Configloader.INSTANCE.deleteGuildConfig(guild, "botautoroles", role.getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/botautorole:removesuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listroles(event, guild);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("botautorole", "Configurates the roles given to every new bot joining in the future!")
				  .addSubcommands(new SubcommandData("add", "Adds a role!").addOptions(new OptionData(OptionType.ROLE, "addrole", "Mention the role you want to add!").setRequired(true)))
				  .addSubcommands(new SubcommandData("remove", "Removes a role!").addOptions(new OptionData(OptionType.ROLE, "removerole", "Mention the role you want to remove!").setRequired(true)))
				  .addSubcommands(new SubcommandData("list", "Displays the roles currently given to every new bot!"));
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to configure, which roles should be given each new joining bot!";
	}
	
	private void listroles(SlashCommandEvent event, Guild guild) {
		final StringBuilder sB = new StringBuilder();
		final String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "botautoroles");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autorole:nobotautoroles")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new bot joins:", "#1\s\s" + guild.getRoleById(currentraw).getAsMention())).queue();
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
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current roles which will be assigned when a new bot joins:", sB.toString())).queue();
	}

}
