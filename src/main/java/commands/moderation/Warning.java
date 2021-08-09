package commands.moderation;

import base.Configloader;
import commands.Command;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Warning implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(Configloader.INSTANCE.getGuildConfig(event.getGuild(), "modrole")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/warn:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			final User user = event.getOption("member").getAsUser();
			String reason;
			if (event.getOption("reason").toString() == "") {
				reason = "~Unknown reason~";
			} else {
				reason = event.getOption("reason").toString();
			}
			Configloader.INSTANCE.addUserConfig(event.getGuild().getMemberById(user.getId()), "warnings", reason);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/warn:success"));
			user.openPrivateChannel().queue((channel) -> {
				 channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(":warning: You have been warned :warning:", "Reason:\n" + reason)).queue();});
			//ModEngine.getInstance().processWarnings(event.getGuild());
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			final User user = event.getOption("member").getAsUser();
			String allwarnings = Configloader.INSTANCE.getUserConfig(event.getGuild().getMemberById(user.getId()), "warnings");
			String[] warnings = allwarnings.split(";");
			StringBuilder sB = new StringBuilder();
			sB.append("Warning-Count:" + warnings.length + "\n");
			for (int i = 0; i < warnings.length; i++) {
				sB.append('#')
				  .append(String.valueOf(i) + "\s")
				  .append(warnings[i]);
				if (i+1 != warnings.length) {
					sB.append(";\n");
				} else {
					sB.append(";");
				}
			}
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Warnings of" + event.getGuild().getMemberById(user.getId()).getAsMention(), sB.toString()));
		}
	}

	@Override
	public CommandData initialize(Guild guild) {
		CommandData command = new CommandData("warning", "Warn a member")
								  .addSubcommands(new SubcommandData("add", "Warns a user and adds a warning to their warnings-list"))
											  .addOptions(new OptionData(OptionType.USER, "member", "The member you want to warn").setRequired(true))
											  .addOptions(new OptionData(OptionType.STRING, "reason", "The reason why you warn the member").setRequired(false))
								  .addSubcommands(new SubcommandData("list", "Shows you the number of warnings a member already has")
										  	  .addOptions(new OptionData(OptionType.USER, "member", "The member you want to check").setRequired(true)));
		return command;
	}

	@Override
	public String getHelp() {
		return "Warn a member for rude behavior etc. The bot will keep track of it and the serveradmin can define automatic punishements when a specific number of warnings is reached!";
	}
}
