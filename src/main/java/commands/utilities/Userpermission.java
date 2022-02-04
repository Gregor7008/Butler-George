package commands.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Userpermission implements Command{
	
	List<Message> msgs = new ArrayList<>();

	@Override
	public void perform(SlashCommandEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		if (!event.getTextChannel().getParent().equals(guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/userpermission:nopermission")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (event.getOption("user") == null && event.getOption("role") == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/userpermission:noargs")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (event.getSubcommandName().equals("reset")) {
			this.defineEdit(null, event, null, false);
			return;
		}
		SelectionMenu menu = SelectionMenu.create("permselection")
				.setPlaceholder("Select the wanted permission")
				.setRequiredRange(1, 1)
				.addOption("View Channel", "vc")
				.addOption("Manage Channel", "mc")
				.addOption("Manage Webhooks", "mw")
				.addOption("Send Messages", "sm")
				.addOption("Create Private Threads", "cpt")
				.addOption("Embed Links", "el")
				.addOption("Attach Files", "af")
				.addOption("Add Reactions", "ar")
				.addOption("Mention @everyone", "me")
				.addOption("Manage Messages", "mm")
				.addOption("Manage Threads", "mt")
				.addOption("Show Message History", "smh")
				.addOption("Use Slash-Commands", "usc")
				.addOption("All Permissions", "apm")
				.build();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/userpermission:selperm"))
				.addActionRow(menu)
				.queue();
		Bot.INSTANCE.getWaiter().waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getTextChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {if (event.getSubcommandName().equals("grant")) {
						  this.defineEdit(e.getSelectedOptions().get(0).getValue(), event, e, true);
					  } else {
						  this.defineEdit(e.getSelectedOptions().get(0).getValue(), event, e, false);
					  }},
				1, TimeUnit.MINUTES,
				() -> {event.getHook().editOriginalEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});	
	}
	
	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("userpermission", "Edits permission in user channels")
				.addSubcommands(new SubcommandData("grant", "Grants permission in a user channel")
						.addOption(OptionType.CHANNEL, "channel_or_category", "The channel or category", true)
						.addOption(OptionType.USER, "user", "The wanted user")
						.addOption(OptionType.ROLE, "role", "The wanted role"))
				.addSubcommands(new SubcommandData("remove", "Removes a permission in a user channel")
						.addOption(OptionType.CHANNEL, "channel_or_category", "The channel or category", true)
						.addOption(OptionType.USER, "user", "The wanted user")
						.addOption(OptionType.ROLE, "role", "The wanted role"))
				.addSubcommands(new SubcommandData("reset", "Resets the permissions in a user channel")
						.addOption(OptionType.CHANNEL, "channel_or_category", "The channel or category", true)
						.addOption(OptionType.USER, "user", "The wanted user")
						.addOption(OptionType.ROLE, "role", "The wanted role"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/userpermission:help");
	}
	
	private void defineEdit(String selected, SlashCommandEvent event, SelectionMenuEvent sme, boolean action) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		IPermissionHolder pholder = null;
		GuildChannel channel = guild.getGuildChannelById(event.getOption("channel_or_category").getAsLong());
		Category category = guild.getCategoryById(event.getOption("channel_or_category").getAsLong());
		try {
			pholder = event.getOption("role").getAsRole();
		} catch (NullPointerException e) {
			pholder = guild.getMember(event.getOption("user").getAsUser());
		}
		if (pholder.equals(guild.getSelfMember())) {
			sme.replyEmbeds(AnswerEngine.getInstance().buildMessage("MUHAHAHAHA", ":rofl: | You really thought that would work?!\nI got administrator permissions, remember?")).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		if (selected == null) {
			this.updatePerms(pholder, channel, selected, action);
			return;
		}
		if (pholder.equals(guild.getPublicRole()) && selected.equals("vc")) {
			sme.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/userpermission:invalid")).queue();
			return;
		}
		if (channel != null) {
			this.updatePerms(pholder, channel, selected, action);
			sme.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/userpermission:remsuccess")).queue();
			return;
		}
		if (category != null) {
			List<GuildChannel> channels = category.getChannels();
			for (int i = 0; i < channels.size(); i++) {
				this.updatePerms(pholder, channels.get(i), selected, action);
			}
			sme.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/userpermission:remsuccess")).queue();
			return;
		}
		sme.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "general:fatal")).queue();
	}
	
	private void updatePerms(IPermissionHolder pholder, GuildChannel channel, String selected, boolean action) {
		if (selected == null) {
			channel.putPermissionOverride(pholder).reset().queue();
			return;
		}
		PermissionOverride current = channel.getPermissionOverride(pholder);
		if (current != null) {
			Collection<Permission> update = this.convertToPerm(selected);
			Collection<Permission> curdeny = current.getDenied();
			Collection<Permission> curallow = current.getAllowed();
			if (action) {
				curallow.forEach(p -> update.add(p));
				channel.putPermissionOverride(pholder).setDeny(curdeny).setAllow(update).queue();
			} else {
				curdeny.forEach(p -> update.add(p));
				channel.putPermissionOverride(pholder).setAllow(curallow).setDeny(update).queue();
			}
		}
	}
	
	private Collection<Permission> convertToPerm(String string) {
		Collection<Permission> perms = new LinkedList<>();
		switch (string) {
		case "vc":
			perms.add(Permission.VIEW_CHANNEL);
			break;
		case "mc":
			perms.add(Permission.MANAGE_CHANNEL);
			break;
		case "mw":
			perms.add(Permission.MANAGE_WEBHOOKS);
			break;
		case "sm":
			perms.add(Permission.MESSAGE_WRITE);
			break;
		case "cpt":
			perms.add(Permission.USE_PRIVATE_THREADS);
			break;
		case "el":
			perms.add(Permission.MESSAGE_EMBED_LINKS);
			break;
		case "af":
			perms.add(Permission.MESSAGE_ATTACH_FILES);
			break;
		case "ar":
			perms.add(Permission.MESSAGE_ADD_REACTION);
			break;
		case "me":
			perms.add(Permission.MESSAGE_MENTION_EVERYONE);
			break;
		case "mm":
			perms.add(Permission.MESSAGE_MANAGE);
			break;
		case "mt":
			perms.add(Permission.MANAGE_THREADS);
			break;
		case "smh":
			perms.add(Permission.MESSAGE_HISTORY);
			break;
		case "usc":
			perms.add(Permission.USE_SLASH_COMMANDS);
			break;
		case "apm":
			Permission.getPermissions(Permission.ALL_VOICE_PERMISSIONS).forEach(e -> perms.add(e));
			Permission.getPermissions(Permission.ALL_TEXT_PERMISSIONS).forEach(e -> perms.add(e));
			perms.add(Permission.MANAGE_WEBHOOKS);
			perms.add(Permission.MANAGE_CHANNEL);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + string);
		}
		return perms;
	}
}