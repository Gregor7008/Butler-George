package functions.slash_commands.utilities;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import functions.slash_commands.SlashCommandList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;

public class Levelbackground implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			Level lv = (Level) SlashCommandList.getHandler("level");
			if (Integer.parseInt(event.getOption("number").getAsString()) > 4 || Integer.parseInt(event.getOption("number").getAsString()) < 0) {
				event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "wrongarg")).queue();
			} else {
				ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("levelbackground", event.getOption("number").getAsInt());
				event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "success")).addFiles(FileUpload.fromData(lv.renderLevelcard(user, guild))).queue();
			}
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listlevelcards(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("levelbackground", "Configure your personal levelbackground")
									  .addSubcommands(new SubcommandData("set", "Set your new levelbackground")
											  					.addOption(OptionType.INTEGER, "number", "The number of your new levelbackground", true),
													  new SubcommandData("list", "List all possible backgrounds"));
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	private void listlevelcards(SlashCommandInteractionEvent event) {
		EmbedBuilder eb0 = new EmbedBuilder();
		EmbedBuilder eb1 = new EmbedBuilder();
		EmbedBuilder eb2 = new EmbedBuilder();
		EmbedBuilder eb3 = new EmbedBuilder();
		EmbedBuilder eb4 = new EmbedBuilder();
		
		eb0.setTitle("Levelbackground 0 (default)");
		eb1.setTitle("Levelbackground 1");
		eb2.setTitle("Levelbackground 2");
		eb3.setTitle("Levelbackground 3");
		eb4.setTitle("Levelbackground 4");
		
		eb0.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
		eb1.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
		eb2.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
		eb3.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
		eb4.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
		
		try {
		    ClassLoader cl = this.getClass().getClassLoader();
			eb0.setImage("attachment://0.png");
			eb1.setImage("attachment://1.png");
			eb2.setImage("attachment://2.png");
			eb3.setImage("attachment://3.png");
			eb4.setImage("attachment://4.png");
			event.replyEmbeds(eb0.build(), eb1.build(), eb2.build(), eb3.build(), eb4.build())
				 .addFiles(FileUpload.fromData(cl.getResourceAsStream("levelcards/0.png"), "0.png"),
						   FileUpload.fromData(cl.getResourceAsStream("levelcards/1.png"), "1.png"),
						   FileUpload.fromData(cl.getResourceAsStream("levelcards/2.png"), "2.png"),
						   FileUpload.fromData(cl.getResourceAsStream("levelcards/3.png"), "3.png"),
						   FileUpload.fromData(cl.getResourceAsStream("levelcards/4.png"), "4.png"))
				 .queue();
		} catch (Exception e) {
			e.printStackTrace();
			event.replyEmbeds(LanguageEngine.getMessageEmbed(null, null, null, "fatal")).queue();
		}
	}
}