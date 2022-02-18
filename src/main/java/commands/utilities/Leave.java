package commands.utilities;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		GuildChannel channel;
		if (event.getOption("channel") != null) {
			channel = event.getOption("channel").getAsGuildChannel();
			if (!ctgid.equals("")) {
				Category ctg = event.getTextChannel().getParent();
				if (ctg.equals(guild.getCategoryById(ctgid))) {
					if (ctg.getChannels().size() <=1) {
						ctg.delete().queue();
					}
					channel.delete().queue();
					return;
				}
			}
		} else {
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
			event.reply("Done...").queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/utilities/leave:invalid")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("leave", "Leave this (or another) channel")
				.addOption(OptionType.CHANNEL, "channel", "If it's a channel you can't run commands in");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/utilities/leave:help");
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