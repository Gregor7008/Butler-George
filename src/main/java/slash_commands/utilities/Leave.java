package slash_commands.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import base.Bot;
import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.ICategorizableChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import slash_commands.assets.CommandEventHandler;

public class Leave implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		GuildChannel channel = event.getGuildChannel();
		if (event.getOption("channel") != null) {
			channel = event.getOption("channel").getAsChannel();
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
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
	
	private User checkCategory(Category category, Guild guild) {
		try {
			return Bot.INSTANCE.jda.getUserById(ConfigLoader.INSTANCE.getFirstGuildLayerConfig(guild, "customchannelcategories").getLong(category.getId()));
		} catch (JSONException e) {
			return null;
		}
	}
}