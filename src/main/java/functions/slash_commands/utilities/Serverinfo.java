
package functions.slash_commands.utilities;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.TimeFormat;

public class Serverinfo implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		Guild guild = event.getGuild();
		String[] titles = LanguageEngine.getRaw(guild, event.getUser(), this, "titles").split(LanguageEngine.SEPERATOR);
		eb.setTitle(titles[0] + "\s" + guild.getName());
		eb.setThumbnail(guild.getIconUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getEffectiveAvatarUrl());
		eb.setFooter(LanguageEngine.buildFooter());
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside:" + titles[1], "`" + guild.getName() + "`", true);
		eb.addField(":registered:" + titles[2], "`" + guild.getId() + "`", true);
		eb.addField(":sunglasses:" + titles[3], "`" + guild.retrieveOwner().complete().getAsMention() + "`", true);
		eb.addField(":calendar:" + titles[4], "`" + TimeFormat.DATE_LONG.format(guild.getTimeCreated()) + "`", true);
		eb.addField(":rocket:" + titles[5], "`" + guild.getBoostCount() + "`", true);
		eb.addField(":trackball:" + titles[6], "`" + guild.getBoostTier() + "`", true);		
		eb.addField(":mens:" + titles[7], "`" + guild.getMemberCount() + "`", true);
		eb.addField(":pager:" + titles[8], "`" + guild.getRoles().size() + "`", true);
		eb.addField(":abacus:" + titles[9], "`" + guild.getChannels().size() + "`", true);
		
		event.replyEmbeds(eb.build()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("serverinfo", "Lists information about this server");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
}