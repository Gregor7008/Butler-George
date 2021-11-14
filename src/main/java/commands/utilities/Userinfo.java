package commands.utilities;

import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Userinfo implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (event.getMember().getRoles().contains(event.getGuild().getRoleById(Configloader.INSTANCE.getGuildConfig(event.getGuild(), "modrole")))) {
			this.listModInfo(event);
			return;
		} else {
			this.listInfo(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("userinfo", "Request the information about a member").addOption(OptionType.USER, "member", "The member the information should be about", false);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/userinfo:help");
	}
	
	private void listModInfo (SlashCommandEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		String booster;
		Member member;
		if (event.getOption("member") == null) {
			member = event.getMember();
		} else {
			member = event.getGuild().getMember(event.getOption("member").getAsUser());
		}
		if (member.getEffectiveName().equals(event.getGuild().getSelfMember().getEffectiveName())) {
			event.reply("You think you're funny or what?").queue();
			return;
		}
		if (member.getTimeBoosted() == null) {
			booster = "false";
		} else {
			booster = "Since\s" + member.getTimeBoosted().format(formatter) + "\s:heart:";
		}
		eb.setTitle("Information about\s" + member.getEffectiveName());
		eb.setThumbnail(member.getUser().getAvatarUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
		eb.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		
		eb.addField(":diamond_shape_with_a_dot_inside:  Name", "`" + member.getUser().getName() + "`", true);
		eb.addField(":name_badge:  Nickname", "`" + member.getEffectiveName() + "`", true);
		eb.addField(":registered:  Discriminator", "`" + member.getUser().getDiscriminator() + "`", true);
		eb.addField(":id:  ID", "`" + member.getUser().getId() + "`", true);
		eb.addField(":robot:  Bot", "`" + String.valueOf(member.getUser().isBot()) + "`", true);
		eb.addField(":rocket:  Booster", "`" + booster + "`", true);
		eb.addField(":calendar:  Joined", "`" + member.getTimeJoined().format(formatter) + "`", true);
		eb.addField(":calendar:  Account created", "`" + member.getUser().getTimeCreated().format(formatter) + "`", true);
		eb.addField(":warning:  Warnings", "`" + String.valueOf(Configloader.INSTANCE.getUserConfig(event.getGuild(), member.getUser(), "warnings").split(";").length - 1) + "`", true);
		eb.addField(":card_index:  Experience", "`" + Configloader.INSTANCE.getUserConfig(event.getGuild(), member.getUser(), "expe") + "`", true);
		eb.addField(":pager:  Level", "`" + Configloader.INSTANCE.getUserConfig(event.getGuild(), member.getUser(), "level") + "`", true);
		//eb.addField(":alarm_clock:  Status", "`" + member.getOnlineStatus().toString() + "`", true);
		eb.addField(":abacus:  Role-Count", "`" + String.valueOf(member.getRoles().size()) + "`", true);
		eb.addField(":arrow_up:  Highest role", "`" + member.getRoles().get(0).getName() + "`", true);
		
		event.replyEmbeds(eb.build()).queue();
	}

	private void listInfo(SlashCommandEvent event) {
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		String booster;
		Member member;
		if (event.getOption("member") == null) {
			member = event.getMember();
		} else {
			member = event.getGuild().getMember(event.getOption("member").getAsUser());
		}
		if (member.getEffectiveName().equals(event.getGuild().getSelfMember().getEffectiveName())) {
			event.reply("You think you're funny or what?").queue();
			return;
		}
		if (member.getTimeBoosted() == null) {
			booster = "false";
		} else {
			booster = "Since\s" + member.getTimeBoosted().format(formatter) + "\s:heart:";
		}
		eb.setTitle("Information about\s" + member.getEffectiveName());
		eb.setThumbnail(member.getUser().getAvatarUrl());
		eb.setAuthor(event.getMember().getEffectiveName(), null, event.getMember().getUser().getAvatarUrl());
		eb.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		
		eb.addField(":diamond_shape_with_a_dot_inside: Name", "`" + member.getEffectiveName() + "`", true);
		eb.addField(":registered: Discriminator", "`" + member.getUser().getDiscriminator() + "`", true);
		eb.addField(":robot: Bot", "`" + String.valueOf(member.getUser().isBot()) + "`", true);
		eb.addField(":rocket: Booster", "`" + booster + "`", true);
		eb.addField(":calendar: Joined", "`" + member.getTimeJoined().format(formatter) + "`", true);
		eb.addField(":card_index: Experience", "`" + Configloader.INSTANCE.getUserConfig(event.getGuild(), member.getUser(), "expe") + "`", true);
		eb.addField(":pager: Level", "`" + Configloader.INSTANCE.getUserConfig(event.getGuild(), member.getUser(), "level") + "`", true);
		//eb.addField(":alarm_clock: Status", "`" + member.getOnlineStatus().toString() + "`", true);
		eb.addField(":abacus: Role-Count", "`" + String.valueOf(member.getRoles().size()) + "`", true);
		eb.addField(":arrow_up: Hoisted role", "`" + member.getRoles().get(0).getName() + "`", true);
		
		event.replyEmbeds(eb.build()).queue();
	}
}
