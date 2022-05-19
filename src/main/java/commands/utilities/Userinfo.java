package commands.utilities;

import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Userinfo implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		if (event.getMember().getRoles().contains(event.getGuild().getRoleById(ConfigLoader.run.getGuildConfig(event.getGuild(), "modrole"))) && 
				!event.getGuild().getPublicRole().hasPermission(event.getGuildChannel(), Permission.VIEW_CHANNEL)) {
			this.listModInfo(event);
		} else {
			this.listInfo(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("userinfo", "Requests information about a user").addOption(OptionType.USER, "user", "The member the information should be about", false);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/utilities/userinfo:help");
	}
	
	private void listModInfo (SlashCommandInteractionEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		String booster;
		Member member;
		String[] titles = AnswerEngine.ae.getRaw(event.getGuild(), event.getUser(), "/commands/utilities/userinfo:titles").split(",");
		if (event.getOption("member") == null) {
			member = event.getMember();
		} else {
			member = event.getGuild().getMember(event.getOption("member").getAsUser());
		}
		if (member.equals(event.getGuild().getSelfMember())) {
			event.reply("You think you're funny or what?").queue();
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
		eb.setFooter(AnswerEngine.ae.footer);
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside:" + titles[2], "`" + member.getUser().getName() + "`", true);
		eb.addField(":name_badge:" + titles[3], "`" + member.getEffectiveName() + "`", true);
		eb.addField(":registered:" + titles[4], "`" + member.getUser().getDiscriminator() + "`", true);
		eb.addField(":id:" + titles[5], "`" + member.getUser().getId() + "`", true);
		eb.addField(":robot:" + titles[6], "`" + String.valueOf(member.getUser().isBot()) + "`", true);
		eb.addField(":rocket:" + titles[7], "`" + booster + "`", true);
		eb.addField(":calendar:" + titles[8], "`" + member.getTimeJoined().format(formatter) + "`", true);
		eb.addField(":calendar:" + titles[9], "`" + member.getUser().getTimeCreated().format(formatter) + "`", true);
		eb.addField(":warning:" + titles[10], "`" + String.valueOf(ConfigLoader.run.getUserConfig(event.getGuild(), member.getUser(), "warnings").split(";").length - 1) + "`", true);
		eb.addField(":card_index:" + titles[11], "`" + ConfigLoader.run.getUserConfig(event.getGuild(), member.getUser(), "expe") + "`", true);
		eb.addField(":pager:" + titles[12], "`" + ConfigLoader.run.getUserConfig(event.getGuild(), member.getUser(), "level") + "`", true);
		//eb.addField(":alarm_clock:" + titles[13], "`" + member.getOnlineStatus().toString() + "`", true);
		eb.addField(":abacus:" + titles[14], "`" + String.valueOf(member.getRoles().size()) + "`", true);
		eb.addField(":arrow_up:" + titles[15], "`" + member.getRoles().get(0).getName() + "`", true);
		
		event.replyEmbeds(eb.build()).queue();
	}

	private void listInfo(SlashCommandInteractionEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		String booster;
		Member member;
		String[] titles = AnswerEngine.ae.getRaw(event.getGuild(), event.getUser(), "/commands/utilities/userinfo:titles").split(",");
		if (event.getOption("member") == null) {
			member = event.getMember();
		} else {
			member = event.getGuild().getMember(event.getOption("member").getAsUser());
		}
		if (member.equals(event.getGuild().getSelfMember())) {
			event.reply("You think you're funny or what?").queue();
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
		eb.setFooter(AnswerEngine.ae.footer);
		eb.setColor(56575);
		
		eb.addField(":diamond_shape_with_a_dot_inside:" + titles[2], "`" + member.getEffectiveName() + "`", true);
		eb.addField(":registered:" + titles[4], "`" + member.getUser().getDiscriminator() + "`", true);
		eb.addField(":robot:" + titles[6], "`" + String.valueOf(member.getUser().isBot()) + "`", true);
		eb.addField(":rocket:" + titles[7], "`" + booster + "`", true);
		eb.addField(":calendar:" + titles[8], "`" + member.getTimeJoined().format(formatter) + "`", true);
		eb.addField(":card_index:" + titles[11], "`" + ConfigLoader.run.getUserConfig(event.getGuild(), member.getUser(), "expe") + "`", true);
		eb.addField(":pager:" + titles[12], "`" + ConfigLoader.run.getUserConfig(event.getGuild(), member.getUser(), "level") + "`", true);
		//eb.addField(":alarm_clock:" + titles[13], "`" + member.getOnlineStatus().toString() + "`", true);
		eb.addField(":abacus:" + titles[14], "`" + String.valueOf(member.getRoles().size()) + "`", true);
		eb.addField(":arrow_up:" + titles[15], "`" + member.getRoles().get(0).getName() + "`", true);
		
		event.replyEmbeds(eb.build()).queue();
	}
}
