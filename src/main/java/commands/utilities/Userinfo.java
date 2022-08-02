package commands.utilities;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Userinfo implements CommandEventHandler {

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

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
	
	private void listInfo (SlashCommandInteractionEvent event, boolean moderator) {
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		String booster;
		Member member;
		String[] titles = LanguageEngine.getRaw(event.getGuild(), event.getUser(), this, "titles").split(",");
		if (event.getOption("user") == null) {
			member = event.getMember();
		} else {
			member = event.getGuild().getMember(event.getOption("user").getAsUser());
		}
		if (member.equals(event.getGuild().getSelfMember())) {
			event.replyEmbeds(LanguageEngine.fetchMessage(event.getGuild(), event.getUser(), this, "6").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (member.getTimeBoosted() == null) {
			booster = "false";
		} else {
			booster = titles[0] + "\s" + member.getTimeBoosted().format(formatter) + "\s:heart:";
		}
		eb.setTitle(titles[1] + "\s" + member.getEffectiveName());
		eb.setThumbnail(member.getUser().getAvatarUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
		eb.setFooter(LanguageEngine.footer);
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside:" + titles[2], "`" + member.getUser().getName() + "`", true);
		eb.addField(":name_badge:" + titles[3], "`" + member.getEffectiveName() + "`", true);
		eb.addField(":registered:" + titles[4], "`" + member.getUser().getDiscriminator() + "`", true);
		if(moderator) {eb.addField(":id:" + titles[5], "`" + member.getUser().getId() + "`", true);}
		eb.addField(":robot:" + titles[6], "`" + String.valueOf(member.getUser().isBot()) + "`", true);
		eb.addField(":rocket:" + titles[7], "`" + booster + "`", true);
		eb.addField(":calendar:" + titles[8], "`" + member.getTimeJoined().format(formatter) + "`", true);
		if(moderator) {eb.addField(":calendar:" + titles[9], "`" + member.getUser().getTimeCreated().format(formatter) + "`", true);
					   eb.addField(":warning:" + titles[10], "`" + String.valueOf(ConfigLoader.getMemberConfig(event.getGuild(), member.getUser()).getJSONArray("warnings").length()) + "`", true);
					   eb.addField(":clock11:" + titles[16], "`" + member.getTimeOutEnd().format(formatter) + "`", true);}
		eb.addField(":card_index:" + titles[11], "`" + String.valueOf(ConfigLoader.getMemberConfig(event.getGuild(), member.getUser()).getInt("experience")) + "`", true);
		eb.addField(":pager:" + titles[12], "`" + String.valueOf(ConfigLoader.getMemberConfig(event.getGuild(), member.getUser()).getInt("level")) + "`", true);
		eb.addField(":alarm_clock:" + titles[13], "`" + member.getOnlineStatus().toString() + "`", true);
		eb.addField(":abacus:" + titles[14], "`" + String.valueOf(member.getRoles().size()) + "`", true);
		if (member.getRoles().size() > 0) {
			eb.addField(":arrow_up:" + titles[15], "`" + member.getRoles().get(0).getName() + "`", true);
		}
		event.replyEmbeds(eb.build()).queue();
	}
}
