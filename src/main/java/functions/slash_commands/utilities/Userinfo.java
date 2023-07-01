package functions.slash_commands.utilities;

import java.util.concurrent.TimeUnit;

import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.TimeFormat;

public class Userinfo implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE) && 
				!event.getGuild().getPublicRole().hasPermission(event.getGuildChannel(), Permission.VIEW_CHANNEL)) {
			this.listInfo(event, true);
		} else {
			this.listInfo(event, false);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("userinfo", "Requests information about a user").addOption(OptionType.USER, "user", "The member the information should be about", false);
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	private void listInfo (SlashCommandInteractionEvent event, boolean moderator) {
		EmbedBuilder eb = new EmbedBuilder();
		String booster, timeoutEnd;
		Member member;
		String[] titles = LanguageEngine.getRaw(event.getGuild(), event.getUser(), this, "titles").split(LanguageEngine.SEPERATOR);
		if (event.getOption("user") == null) {
			member = event.getMember();
		} else {
			member = event.getGuild().getMember(event.getOption("user").getAsUser());
		}
		if (member.equals(event.getGuild().getSelfMember())) {
			event.replyEmbeds(LanguageEngine.getMessageEmbed(event.getGuild(), event.getUser(), this, "6")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (member.getTimeBoosted() == null) {
			booster = "false";
		} else {
			booster = titles[0] + "\s" + TimeFormat.DATE_LONG.format(member.getTimeBoosted()) + "\s:heart:";
		}
		 if (member.getTimeOutEnd() == null) {
		     timeoutEnd = "--";
		 } else {
		     timeoutEnd = TimeFormat.DATE_TIME_SHORT.format(member.getTimeOutEnd());
		 }
		eb.setTitle(titles[1] + "\s" + member.getEffectiveName());
		eb.setThumbnail(member.getUser().getEffectiveAvatarUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getEffectiveAvatarUrl());
		eb.setFooter(LanguageEngine.buildFooter());
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside:" + titles[2], "`" + member.getUser().getName() + "`", true);
		eb.addField(":name_badge:" + titles[3], "`" + member.getAsMention() + "`", true);
		if(moderator) {eb.addField(":id:" + titles[5], "`" + member.getUser().getId() + "`", true);}
		eb.addField(":robot:" + titles[6], "`" + String.valueOf(member.getUser().isBot()) + "`", true);
		eb.addField(":rocket:" + titles[7], "`" + booster + "`", true);
		eb.addField(":calendar:" + titles[8], "`" + TimeFormat.DATE_LONG.format(member.getTimeJoined()) + "`", true);
		if(moderator) {eb.addField(":calendar:" + titles[9], "`" + TimeFormat.DATE_LONG.format(member.getUser().getTimeCreated()) + "`", true);
					   eb.addField(":warning:" + titles[10], "`" + String.valueOf(ConfigLoader.INSTANCE.getMemberConfig(event.getGuild(), member.getUser()).getJSONArray("warnings").length()) + "`", true);
					   eb.addField(":clock11:" + titles[15], "`" + timeoutEnd + "`", true);}
		eb.addField(":card_index:" + titles[11], "`" + String.valueOf(ConfigLoader.INSTANCE.getMemberConfig(event.getGuild(), member.getUser()).getInt("experience")) + "`", true);
		eb.addField(":pager:" + titles[12], "`" + String.valueOf(ConfigLoader.INSTANCE.getMemberConfig(event.getGuild(), member.getUser()).getInt("level")) + "`", true);
		eb.addField(":abacus:" + titles[13], "`" + String.valueOf(member.getRoles().size()) + "`", true);
		if (member.getRoles().size() > 0) {
			eb.addField(":arrow_up:" + titles[14], "`" + member.getRoles().get(0).getName() + "`", true);
		}
		event.replyEmbeds(eb.build()).queue();
	}
}