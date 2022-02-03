package commands.textcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import base.Bot;
import commands.TextCommand;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Addpermission implements TextCommand{
	
	List<Message> msgs = new ArrayList<>();

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		final User user = event.getAuthor();
		final Guild guild = event.getGuild();
		final TextChannel channel = event.getChannel();
		msgs.add(event.getMessage());
		if (!channel.getParent().equals(guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory")))) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/textcommands/addpermission:nopermission")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
			this.cleanup();
			return;
		}
		if (argument.equals("")) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/textcommands/addpermission:noargs")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
			this.cleanup();
			return;
		}
		SelectionMenu menu = SelectionMenu.create("permselection")
				.setPlaceholder("Select the wanted permission")
				.setRequiredRange(1, 1)
				.addOption("View Channel", "vc")
				.addOption("Manage Channel", "mc")
				.addOption("Manage Webhooks", "mw")
				.addOption("Send Messages", "ms")
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
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/textcommands/addpermission:selperm"))
				.setActionRow(menu)
				.queue(m -> msgs.add(m));
		Bot.INSTANCE.getWaiter().waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {try {
						this.assignMemberPerms(e.getSelectedOptions().get(0).getValue(), event.getMessage().getMentionedMembers().get(0));
						return;
					  } catch (IndexOutOfBoundsException e2) {}
					  try {
						this.assignRolePerms(e.getSelectedOptions().get(0).getValue(), event.getMessage().getMentionedRoles().get(0));
						return;
					  } catch (IndexOutOfBoundsException e3) {}
					  this.cleanup();
					  channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "general:fatal")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});	
	}
	
	private void cleanup() {
		for (int i = 0; i < msgs.size(); i++) {
			msgs.get(i).delete().queue();
		}
	}
	
	private void assignMemberPerms(String selected, Member member) {
		
	}
	
	private void assignRolePerms(String selected, Role role) {
		
	}
}