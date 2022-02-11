package commands.utilities;

import java.io.File;
import java.util.List;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Leave implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		String ctgid = Configloader.INSTANCE.getUserConfig(guild, user, "cccategory");
		if (!ctgid.equals("")) {
			if (event.getTextChannel().getParent().equals(guild.getCategoryById(ctgid))) {
				List<GuildChannel> channels = guild.getCategoryById(ctgid).getChannels();
				for (int i = 0; i < channels.size(); i++) {
					channels.get(i).delete().queue();
				}
				guild.getCategoryById(ctgid).delete().queue();
				return;
			}
		}
		GuildChannel channel;
		if (event.getOption("channel") != null) {
			channel = event.getOption("channel").getAsGuildChannel();
		} else {
			channel = event.getGuildChannel();
		}
		if (guild.getCategoryById(channel.getId()) != null) {
			Category ctgy = guild.getCategoryById(channel.getId());
			if (this.checkCategory(ctgy, guild)) {
				List<GuildChannel> channels = ctgy.getChannels();
				for (int i = 0; i < channels.size(); i++) {
					channels.get(i).getManager().removePermissionOverride(event.getMember()).queue();
				}
				event.reply("Done...").queue(r -> r.deleteOriginal().queue());
				return;
			}
		}
		if (this.checkCategory(channel.getParent(), guild)) {
			channel.getManager().removePermissionOverride(event.getMember()).queue();
			event.reply("Done...").queue(r -> r.deleteOriginal().queue());
			return;
		}
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/leave:invalid")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("leave", "Leave this (or another) channel")
				.addOption(OptionType.CHANNEL, "channel", "If it's a channel you can't run commands in");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/leave:help");
	}
	
	private boolean checkCategory(Category category, Guild guild) {
		File guilddir = new File(Bot.environment + "/configs/user/" + guild.getId());
		File[] filelist = guilddir.listFiles();
		for (int i = 0; i < filelist.length; i++) {
			String[] temp1 = filelist[i].getName().split(".properties");
			User cuser = Bot.INSTANCE.jda.retrieveUserById(temp1[0]).complete();
			String ccid = Configloader.INSTANCE.getUserConfig(guild, cuser, "cccategory");
			if (!ccid.equals("")) {
				if (category.equals(guild.getCategoryById(ccid))) {
					return true;
				}
			}
		}
		return false;
	}
}