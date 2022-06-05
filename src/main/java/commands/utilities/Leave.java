package commands.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Leave implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		Long ctgid = ConfigLoader.run.getUserConfig(guild, user).getLong("customchannelcategory");
		GuildChannel channel;
		if (event.getOption("channel") != null) {
			channel = event.getOption("channel").getAsGuildChannel();
			if (ctgid == 0) {
				Category ctg = event.getTextChannel().getParentCategory();
				if (ctg.equals(guild.getCategoryById(ctgid))) {
					if (ctg.getChannels().size() <=1) {
						ctg.delete().queue();
					}
					channel.delete().queue();
					return;
				}
			}
		} else {
			if (ctgid == 0) {
				if (event.getTextChannel().getParentCategory().equals(guild.getCategoryById(ctgid))) {
					List<GuildChannel> channels = guild.getCategoryById(ctgid).getChannels();
					for (int i = 0; i < channels.size(); i++) {
						channels.get(i).delete().queue();
					}
					guild.getCategoryById(ctgid).delete().queue();
					return;
				}
			}
			channel = event.getGuildChannel();
		}
		if (guild.getCategoryById(channel.getId()) != null) {
			Category ctgy = guild.getCategoryById(channel.getId());
			if (this.checkCategory(ctgy, guild)) {
				List<GuildChannel> channels = ctgy.getChannels();
				for (int i = 0; i < channels.size(); i++) {
					channels.get(i).getPermissionContainer().getManager().removePermissionOverride(event.getMember()).queue();
				}
				event.reply("Done...").queue(r -> r.deleteOriginal().queue());
				return;
			}
		}
		ICategorizableChannel temp = (ICategorizableChannel) channel;
		if (this.checkCategory(temp.getParentCategory(), guild)) {
			channel.getPermissionContainer().getManager().removePermissionOverride(event.getMember()).queue();
			event.reply("Done...").queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/utilities/leave:invalid").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("leave", "Leave this (or another) channel")
				.addOption(OptionType.CHANNEL, "channel", "If it's a channel you can't run commands in");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.run.getRaw(guild, user, "/commands/utilities/leave:help");
	}
	
	private boolean checkCategory(Category category, Guild guild) {
		try {
			Bot.run.jda.getUserById(ConfigLoader.run.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId()));
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
}