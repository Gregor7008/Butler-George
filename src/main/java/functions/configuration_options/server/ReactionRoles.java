package functions.configuration_options.server;

import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import assets.base.AwaitTask;
import assets.functions.ConfigurationEvent;
import assets.functions.ConfigurationEventHandler;
import assets.functions.ConfigurationOptionData;
import assets.functions.ConfigurationSubOptionData;
import base.Bot;
import engines.base.LanguageEngine;
import engines.configs.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class ReactionRoles implements ConfigurationEventHandler {

	private Guild guild;
	private User user;
	private Message message;
	private TextChannel channel;
	int progress = 0;

	@Override
	public void execute(ConfigurationEvent event) {
		this.guild = event.getGuild();
		this.user = event.getUser();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel")).queue();
		//TODO Adept selection system to select menus
		AwaitTask.forMessageReceival(guild, user, event.getChannel(),
				e -> {
					if (!e.getMessage().getMentions().getChannels().isEmpty()) {
							return e.getMessage().getMentions().getChannels().get(0).getType() == ChannelType.TEXT;
						} else {
							return false;
						}
					},
				e -> {
					this.channel = (TextChannel) e.getMessage().getMentions().getChannels().get(0);
					if (event.getSubOperation().equals("list")) {
						//TODO Implement list of reactionroles
					} else {
						event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defmessage")).queue();
						AwaitTask.forMessageReceival(guild, user, event.getChannel(),
								m -> {
									try {
										message = channel.retrieveMessageById(m.getMessage().getContentRaw()).complete();
										return message != null;
									} catch (IllegalArgumentException ex) {return false;}
								},
								m -> {
									if (event.getSubOperation().equals("add")) {
										this.defineAddRoles(event.getMessage());
									} else if (event.getSubOperation().equals("remove")) {
										ConfigLoader.INSTANCE.getReactionChannelConfig(guild, channel.getId()).remove(message.getId());
										event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
										channel.retrieveMessageById(message.getId()).complete().clearReactions().queue();
									} else if (event.getSubOperation().equals("delete")) {
										event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineDelEmoji")).queue();
										AwaitTask.forReactionAdding(guild, user, event.getMessage(),
												r -> {
													event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")
															.replaceDescription("{emoji}", r.getReaction().getEmoji().getFormatted())).queue(a -> a.delete().queueAfter(3, TimeUnit.SECONDS));
													JSONObject actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channel.getId(), message.getId());
													actions.remove(r.getReaction().getEmoji().getFormatted());
													channel.retrieveMessageById(message.getId()).complete().clearReactions(r.getReaction().getEmoji()).queue();
												}).append();
									}
								}, null).append();
					}

				}, null).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("ReactionRoles")
													.setInfo("Configure reactions to give or remove a role")
													.setSubOptions(new ConfigurationSubOptionData[] {
														new ConfigurationSubOptionData("add", "Add new reactionroles to a message"),
														new ConfigurationSubOptionData("delete", "Deactivate and delete a reactionrole from a message"),
														new ConfigurationSubOptionData("remove", "Remove all reactionroles from a message"),
														new ConfigurationSubOptionData("list", "List all active reactionroles")
													})
													.setRequiredPermissions(Permission.MANAGE_ROLES);
		return configurationOptionData;
	}
	
	@Override
	public List<Permission> getRequiredPermissions() {
		return List.of(Permission.MANAGE_ROLES, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE);
	}

	private void defineAddRoles(Message msg) {
		msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineAddRoles")).complete();
		ConfigLoader.INSTANCE.createReactionMessageConfig(guild, channel.getId(), message.getId());
		AwaitTask.forMessageReceival(guild, user, message.getChannel(),
							e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
							e -> {List<Role> roles = e.getMessage().getMentions().getRoles();
								  this.defineAddEmojis(roles, msg);}, null).append();
	}
	
	private void defineAddEmojis(List<Role> roles, Message msg) {
		Role role = roles.get(progress);
		Message response = msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defineAddEmojis").replaceDescription("{role}", role.getAsMention())).complete();
		AwaitTask.forReactionAdding(guild, user, response,
				e -> {ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channel.getId(), message.getId()).put(e.getReaction().getEmoji().getFormatted(), role.getId());
					  progress++;
					  response.clearReactions().queue();
					  if (progress < roles.size()) {
						  this.defineAddEmojis(roles, response);
					  } else {
						  this.addReactions(response);
					  }}).append();		
	}
	
	private void addReactions(Message msg) {
		Message response = msg.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "adding")).complete();
		Set<String> actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channel.getId(), message.getId()).keySet();
		actions.forEach(e -> channel.retrieveMessageById(message.getId()).complete().addReaction(Emoji.fromUnicode(e)).queue());
		Bot.INSTANCE.getTimer().schedule(new TimerTask() {
			@Override
			public void run() {
				response.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")).queue();
			}
		}, 1500);
	}
}