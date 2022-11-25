package functions.configuration_options.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import assets.base.AwaitTask;
import assets.functions.ConfigurationEvent;
import assets.functions.ConfigurationEventHandler;
import assets.functions.ConfigurationOptionData;
import assets.functions.ConfigurationSubOptionData;
import engines.base.CentralTimer;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

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
		StringSelectMenu.Builder menu = StringSelectMenu.create("selchnl")
				.setPlaceholder("Select a channel")
				.setRequiredRange(1, 1);
		List<TextChannel> availableChannels = guild.getTextChannels().stream().filter(channel -> guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)).toList();
		availableChannels.forEach(channel -> {
			if (channel.getTopic() != null) {
				menu.addOption(channel.getName(), channel.getId(), channel.getTopic());
			} else {
				menu.addOption(channel.getName(), channel.getId());
			}
		});
		event.getMessage().editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "defchannel")).setActionRow(menu.build()).queue();
		AwaitTask.forStringSelectInteraction(guild, user, event.getMessage(),
				e -> {
					this.channel = guild.getTextChannelById(e.getSelectedOptions().get(0).getValue());
					if (event.getSubOperation().equals("list")) {
						JSONObject channelConfig = ConfigLoader.INSTANCE.getReactionChannelConfig(guild, channel.getId());
						if (channelConfig != null) {
							e.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "list")
									.replaceDescription("{list}", this.listReactionroles(channel, null))).setComponents().queue();
						} else {
							e.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "noreactionrole")).setComponents().queue();
						}
					} else {
						if (!event.getSubOperation().equals("add")) {
							JSONObject channelConfig = ConfigLoader.INSTANCE.getReactionChannelConfig(guild, channel.getId());
							if (channelConfig == null) {
								event.getMessage().editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "noreactionrole")).queue();
								return;
							}
						}
						e.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "defmessage")).setComponents().queue();
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
									} else {
										try {
											ConfigLoader.INSTANCE.getReactionChannelConfig(guild, channel.getId()).getJSONObject(message.getId());
										} catch (JSONException ex) {
											event.getMessage().editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "norrmessage")).queue();
											return;
										}
										if (event.getSubOperation().equals("remove")) {
											ConfigLoader.INSTANCE.getReactionChannelConfig(guild, channel.getId()).remove(message.getId());
											event.getMessage().editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "remsuccess")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
											channel.retrieveMessageById(message.getId()).complete().clearReactions().queue();
										} else if (event.getSubOperation().equals("delete")) {
											event.getMessage().editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "defineDelEmoji")
													.replaceDescription("{list}", this.listReactionroles(channel, message))).queue();
											AwaitTask.forReactionAdding(guild, user, event.getMessage(),
													r -> {
														event.getMessage().editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "delsuccess")
																.replaceDescription("{emoji}", r.getReaction().getEmoji().getFormatted())).queue(a -> a.delete().queueAfter(3, TimeUnit.SECONDS));
														JSONObject actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channel.getId(), message.getId());
														actions.remove(r.getReaction().getEmoji().getFormatted());
														channel.retrieveMessageById(message.getId()).complete().clearReactions(r.getReaction().getEmoji()).queue();
													}).append();
										}
									}
								}, null).append();
					}
				}).append();
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
		msg.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "defineAddRoles")).complete();
		ConfigLoader.INSTANCE.createReactionMessageConfig(guild, channel.getId(), message.getId());
		AwaitTask.forMessageReceival(guild, user, message.getChannel(),
				e -> {
					return !e.getMessage().getMentions().getRoles().isEmpty();
				},
				e -> {
					List<Role> roles = e.getMessage().getMentions().getRoles();
					this.defineAddEmojis(roles, msg);
				}, null).append();
	}

	private void defineAddEmojis(List<Role> roles, Message msg) {
		Role role = roles.get(progress);
		Message response = msg.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "defineAddEmojis").replaceDescription("{role}", role.getAsMention())).complete();
		AwaitTask.forReactionAdding(guild, user, response,
				e -> {
					response.clearReactions().queue();
					CustomEmoji customEmoji = null;
					boolean validEmoji = true;
					try {
						customEmoji = e.getReaction().getEmoji().asCustom();
					} catch (IllegalStateException ex) {}
					if (customEmoji != null) {
						if (e.getJDA().getEmojiById(customEmoji.getIdLong()) == null) {
							validEmoji = false;
						}
					}
					if (validEmoji) {
						ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channel.getId(), message.getId()).put(e.getReaction().getEmoji().getFormatted(), role.getIdLong());
						progress++;
						if (progress < roles.size()) {
							this.defineAddEmojis(roles, response);
						} else {
							this.addReactions(response);
						}
					} else {
						response.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "invalidEmoji")).queue();
						CentralTimer.get().schedule(() -> defineAddEmojis(roles, response), TimeUnit.MILLISECONDS, 1500);
					}
				}).append();		
	}

	private void addReactions(Message msg) {
		Message response = msg.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "adding")).complete();
		Set<String> actions = ConfigLoader.INSTANCE.getReactionMessageConfig(guild, channel.getId(), message.getId()).keySet();
		actions.forEach(e -> channel.retrieveMessageById(message.getId()).complete().addReaction(Emoji.fromFormatted(e)).queue());
		CentralTimer.get().schedule(() -> response.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "success")).queue(), TimeUnit.MILLISECONDS, 1500);
	}

	private String listReactionroles(TextChannel channel, Message message) {
		StringBuilder sB = new StringBuilder();
		JSONObject channelConfig = ConfigLoader.INSTANCE.getReactionChannelConfig(guild, channel.getId());
		List<String> messageIDs = new ArrayList<>();
		if (message != null) {
			messageIDs.add(message.getId());
		} else {
			messageIDs.addAll(channelConfig.keySet());
		}
		for (int a = 0; a < messageIDs.size(); a++) {
			String messageID = messageIDs.get(a);
			sB.append("\n**" + messageID + ":**\n");
			JSONObject messageConfig = channelConfig.getJSONObject(messageID);
			List<String> reactionRoleEmojis = new ArrayList<>();
			reactionRoleEmojis.addAll(messageConfig.keySet());
			for (int i = 0; i < reactionRoleEmojis.size(); i++) {
				String reactionRoleEmoji = reactionRoleEmojis.get(i);
				sB.append("#" + String.valueOf(i+1) + " ");
				sB.append(reactionRoleEmoji + " => " + guild.getRoleById(messageConfig.getLong(reactionRoleEmoji)).getAsMention());
				if (i + 1 < reactionRoleEmojis.size()) {
					sB.append("\n");
				}
			}
		}
		return sB.toString();
	}
}