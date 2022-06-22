package commands.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import base.Bot;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Leave implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		GuildChannel channel = event.getGuildChannel();
		if (event.getOption("channel") != null) {
			channel = event.getOption("channel").getAsGuildChannel();
		}
		if (guild.getCategoryById(channel.getId()) != null) {
			Category ctgy = guild.getCategoryById(channel.getId());
			User catOwner = this.checkCategory(ctgy, guild);
			if (catOwner != null) {
				List<GuildChannel> channels = ctgy.getChannels();
				if (catOwner.equals(user)) {
					for (int i = 0; i < channels.size(); i++) {
						channels.get(i).delete().queue();
					}
					ctgy.delete().queue();
					event.reply("You deleted your category!").queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
					return;
				} else {
					for (int i = 0; i < channels.size(); i++) {
						channels.get(i).getPermissionContainer().getManager().removePermissionOverride(event.getMember()).queue();
					}
					ctgy.getPermissionContainer().getManager().removePermissionOverride(event.getMember()).queue();
					event.reply("You left the category of " + guild.getMember(catOwner).getEffectiveName() + "!").queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
					return;
				}
			}
		}
		try {
			ICategorizableChannel temp = (ICategorizableChannel) channel;
			User catOwner = this.checkCategory(temp.getParentCategory(), guild);
			if (catOwner != null) {
				if (catOwner.equals(user)) {
					String channelName = channel.getName();
					channel.delete().queue();
					event.reply("You deleted your channel called " + channelName + "!").queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
					return;
				} else {
					channel.getPermissionContainer().getManager().removePermissionOverride(event.getMember()).queue();
					event.reply("You left the channel " + channel.getAsMention() + " by " + guild.getMember(catOwner).getEffectiveName() + "!").queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
					return;
				}
			}
		} catch (ClassCastException e) {}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "invalid").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("leave", "Leave this (or another) channel")
									  .addOption(OptionType.CHANNEL, "channel", "If it's a channel you can't run commands in");
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
	
	private User checkCategory(Category category, Guild guild) {
		try {
			return Bot.run.jda.getUserById(ConfigLoader.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId()));
		} catch (JSONException e) {
			return null;
		}
	}
}