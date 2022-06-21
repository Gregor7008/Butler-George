
package commands.utilities;

import java.time.format.DateTimeFormatter;

import components.base.LanguageEngine;
import components.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Serverinfo implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		Guild guild = event.getGuild();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		String[] titles = LanguageEngine.getRaw(guild, event.getUser(), this, "titles").split(",");
		eb.setTitle(titles[0] + "\s" + guild.getName());
		eb.setThumbnail(guild.getIconUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
		eb.setFooter(LanguageEngine.footer);
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside:" + titles[1], "`" + guild.getName() + "`", true);
		eb.addField(":registered:" + titles[2], "`" + guild.getId() + "`", true);
		eb.addField(":sunglasses:" + titles[3], "`" + guild.getOwner().getEffectiveName() + "`", true);
		eb.addField(":calendar:" + titles[4], "`" + guild.getTimeCreated().format(formatter) + "`", true);
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
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
}