
package commands.utilities;

import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Serverinfo implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		Guild guild = event.getGuild();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		eb.setTitle("Information about\s" + guild.getName());
		eb.setThumbnail(guild.getIconUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
		eb.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside: Name", "`" + guild.getName() + "`", true);
		eb.addField(":registered: ID", "`" + guild.getId() + "`", true);
		eb.addField(":sunglasses: Owner", "`" + guild.getOwner().getEffectiveName() + "`", true);
		eb.addField(":calendar: Created", "`" + guild.getTimeCreated().format(formatter) + "`", true);
		eb.addField(":rocket: Boosters", "`" + guild.getBoostCount() + "`", true);
		eb.addField(":trackball: Boost-Level", "`" + guild.getBoostTier() + "`", true);		
		eb.addField(":mens: Members", "`" + guild.getMemberCount() + "`", true);
		eb.addField(":pager: Role-Count", "`" + guild.getRoles().size() + "`", true);
		eb.addField(":abacus: Channel-Count", "`" + guild.getChannels().size() + "`", true);
		
		event.replyEmbeds(eb.build()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("serverinfo", "Lists information about this server");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/serverinfo:help");
	}

}
