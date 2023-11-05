package base;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assets.data.MemberData;
import assets.data.single.AutoMessageData;
import assets.data.single.ModMailData;
import assets.data.single.ReactionRoleData;
import assets.functions.MessageContextEventHandler;
import assets.functions.SlashCommandEventHandler;
import assets.functions.UserContextEventHandler;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import engines.data.ConfigLoader;
import functions.context_menu_commands.MessageContextCommandList;
import functions.context_menu_commands.UserContextCommandList;
import functions.slash_commands.SlashCommandList;
import functions.slash_commands.support.Modmail;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class EventProcessor extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent event) {
		CommandListUpdateAction clua = event.getJDA().updateCommands();
		SlashCommandList.getCommandData().forEach(data -> {
			clua.addCommands(data);
		});
		UserContextCommandList.getCommandData().forEach(data -> {
			clua.addCommands(data);
		});
		MessageContextCommandList.getCommandData().forEach(data -> {
			clua.addCommands(data);
		});
		clua.queue();
	    List<Guild> guilds = event.getJDA().getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			Guild guild = guilds.get(i);
			try {
				Message message = ConfigLoader.get().getGuildData(guild).getOfflineMessage();
				if (message != null) {
					message.delete().queue();
				}
			} catch (JSONException e) {}
			ConfigLoader.get().getGuildData(guild).setOfflineMessage(null);
		}
	}
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		GUI.INSTANCE.increaseExecutionsCounter();
		SlashCommandEventHandler slashCommandEventHandler = SlashCommandList.getHandler(event.getName().toLowerCase());
		if (slashCommandEventHandler != null) {
			try {
				slashCommandEventHandler.execute(event);
			} catch (InsufficientPermissionException e) {
				MessageEmbed embed = LanguageEngine.getMessageEmbed(event.getGuild(), event.getUser(), null, "insufficientperms")
						.replaceDescription("{permissions}", e.getPermission().getName().toLowerCase());
				if (event.isAcknowledged()) {
					event.getHook().retrieveOriginal().complete().editMessageEmbeds(embed).queue();
				} else {
					event.replyEmbeds(embed).queue();
				}
			}
		}
	}

	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		GUI.INSTANCE.increaseExecutionsCounter();
		UserContextEventHandler userContextEventHandler = UserContextCommandList.getHandler(event.getName().toLowerCase());
		if (userContextEventHandler != null) {
			try {
				userContextEventHandler.execute(event);
			} catch (InsufficientPermissionException e) {
				MessageEmbed embed = LanguageEngine.getMessageEmbed(event.getGuild(), event.getUser(), null, "insufficientperms")
						.replaceDescription("{permissions}", e.getPermission().getName().toLowerCase());
				if (event.isAcknowledged()) {
					event.getHook().retrieveOriginal().complete().editMessageEmbeds(embed).queue();
				} else {
					event.replyEmbeds(embed).queue();
				}
			}
		}
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		if (event.isFromGuild()) {
			GUI.INSTANCE.increaseExecutionsCounter();
			MessageContextEventHandler messageContextEventHandler = MessageContextCommandList.getHandler(event.getName().toLowerCase());
			if (messageContextEventHandler != null) {
				try {
					messageContextEventHandler.execute(event);
				} catch (InsufficientPermissionException e) {
					MessageEmbed embed = LanguageEngine.getMessageEmbed(event.getGuild(), event.getUser(), null, "insufficientperms")
							.replaceDescription("{permissions}", e.getPermission().getName().toLowerCase());
					if (event.isAcknowledged()) {
						event.getHook().retrieveOriginal().complete().editMessageEmbeds(embed).queue();
					} else {
						event.replyEmbeds(embed).queue();
					}
				}
			}
		}
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		final User user = event.getAuthor();
		if (event.isFromGuild()) {
			final Guild guild = event.getGuild();
			final ModMailData modmailData = ConfigLoader.get().getGuildData(guild).getModMail(event.getChannel().getIdLong());
			if (modmailData != null) {
				final User ticketOwner = modmailData.getUser();
				final ModMailData selectedTicket = ConfigLoader.get().getUserData(ticketOwner).getSelectedModMail();
				if (selectedTicket != null
						&& selectedTicket.getGuildId() == guild.getIdLong()
						&& selectedTicket.getGuildChannelId() == modmailData.getGuildChannelId()) {
					Toolbox.forwardMessage(ticketOwner.openPrivateChannel().complete(), event.getMessage());
					modmailData.setLastGuildMessage(event.getMessage());
				}
			}
		} else {
			final ModMailData selectedTicket = ConfigLoader.get().getUserData(user).getSelectedModMail();
			if (selectedTicket == null) {
				event.getChannel().sendMessageEmbeds(LanguageEngine.getMessageEmbed(null, user, this, "noticket")).queue();
			} else {
				Toolbox.forwardMessage(selectedTicket.getGuildChannel(), event.getMessage());
				selectedTicket.setLastUserMessage(event.getMessage());
			}
		}
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		GUI.INSTANCE.increaseMemberCounter();
		final Guild guild = event.getGuild();
		if (event.getMember().getUser().isBot()) {
			List<Role> botautoroles = ConfigLoader.get().getGuildData(guild).getBotAutoRoles();
			if (!botautoroles.isEmpty()) {
				for (int i = 0; i < botautoroles.size(); i++) {
					guild.addRoleToMember(event.getMember(), botautoroles.get(i)).queue();
				}
			}
		} else {
		    List<Role> autoroles = ConfigLoader.get().getGuildData(guild).getUserAutoRoles();
			if (!autoroles.isEmpty()) {
				for (int i = 0; i < autoroles.size(); i++) {
					guild.addRoleToMember(event.getMember(), autoroles.get(i)).queue();
				}
			}
			AutoMessageData welcomemsg = ConfigLoader.get().getGuildData(guild).getWelcomeMessage();
			if (welcomemsg != null) {
				if (welcomemsg.isActivated()) {
					welcomemsg.buildMessage(event.getUser()).queue();
				}
			}
		}
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		GUI.INSTANCE.decreaseMemberCounter();
		if (event.getUser().isBot()) {
			return;
		}
		AutoMessageData goodbyemsg = ConfigLoader.get().getGuildData(event.getGuild()).getGoodbyeMessage();
		if (goodbyemsg != null) {
            if (goodbyemsg.isActivated()) {
                goodbyemsg.buildMessage(event.getUser()).queue();
            }
        }
	}
	
	@Override
	public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		if (event.getNewTimeBoosted() != null) {
		    AutoMessageData boostmsg = ConfigLoader.get().getGuildData(event.getGuild()).getBoostMessage();
	        if (boostmsg != null) {
	            if (boostmsg.isActivated()) {
	                boostmsg.buildMessage(event.getUser()).queue();
	            }
	        }
		}
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		GUI.INSTANCE.updateStatistics();
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		GUI.INSTANCE.updateStatistics();
	}
	
	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		if (event.getChannelJoined() != null) {
		    this.managej2cjoin(event.getGuild(), event.getMember(), event.getChannelJoined());
		}
		if (event.getChannelLeft() != null) {
		    this.managej2cleave(event.getGuild(), event.getMember().getUser(), event.getChannelLeft());
		}
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser().isBot() || !event.isFromGuild()) {
			return;
		}
		final long channelId = event.getChannel().getIdLong();
		final long messageId = event.getMessageIdLong();
		final Guild guild = event.getGuild();
		final ReactionRoleData reactionRoleData = ConfigLoader.get().getGuildData(guild).getReactionRole(channelId, messageId);
		if (reactionRoleData != null) {
		    reactionRoleData.onReactionAdd(event.getEmoji().getFormatted(), event.getMember());
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		if (event.getUser().isBot() || !event.isFromGuild()) {
			return;
		}
		final long channelId = event.getChannel().getIdLong();
		final long messageId = event.getMessageIdLong();
		final Guild guild = event.getGuild();
		final ReactionRoleData reactionRoleData = ConfigLoader.get().getGuildData(guild).getReactionRole(channelId, messageId);
        if (reactionRoleData != null) {
            reactionRoleData.onReactionRemove(event.getEmoji().getFormatted(), event.getMember());
        }
	}
	
	@Override
	public void onGuildMemberUpdateTimeOut(GuildMemberUpdateTimeOutEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		final User target = event.getEntity().getUser();
		final Guild guild = event.getGuild();
		MemberData targetData = ConfigLoader.get().getMemberData(guild, target);
		if (event.getNewTimeOutEnd() == null) {
		    if (!event.getUser().isSystem()) {
		        targetData.setPermanentlyMuted(false);
		    } else if (targetData.isPermanentlyMuted()) {
		        guild.getMember(target).timeoutFor(27, TimeUnit.DAYS).queue();
		    }
		    targetData.setTemporarilyMutedUntil(OffsetDateTime.now().minusDays(1L));
		} else {
			if (targetData.isTemporarilyMuted()) {
			    if (targetData.isTemporarilyMutedUntil().isBefore(event.getNewTimeOutEnd())) {
			        targetData.setTemporarilyMutedUntil(event.getNewTimeOutEnd());
			    }
			} else {
			    targetData.setTemporarilyMutedUntil(event.getNewTimeOutEnd());
			}
		}
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		if (event.isFromGuild()) {
			final Guild guild = event.getGuild();
			ConcurrentHashMap<Long,ModMailData> modmails = ConfigLoader.get().getGuildData(guild).getModmailIds();
			final long channelID = event.getChannel().getIdLong();
			if (modmails.keySet().contains(channelID)) {
				final String buttonIdCriteria = String.valueOf(event.getChannel().getIdLong() + guild.getIdLong());
				final String buttonId = event.getComponentId();
				ModMailData modmail = modmails.get(channelID);
				final Modmail modmailCommandHandler = (Modmail) SlashCommandList.getHandler("modmail");
				final User target = modmail.getUser();
				if (buttonId.equals(buttonIdCriteria + "_close")) {
					modmailCommandHandler.close(event, guild, target, modmail.getTicketId());
				} else if (buttonId.equals(buttonIdCriteria + "_confirmclose")) {
					modmailCommandHandler.confirmclose(event, guild, target, modmail.getTicketId());
				} else if (buttonId.equals(buttonIdCriteria + "_denyclose")) {
					modmail.getFeedbackMessage().delete().queue();
					target.openPrivateChannel().complete().sendMessageEmbeds(LanguageEngine.getMessageEmbed(guild, target, modmailCommandHandler, "closeDeniedPrivate")
							.replaceDescription("{guild}", guild.getName())
							.replaceDescription("{title}", modmail.getTitle())).queue();
					event.editMessageEmbeds(LanguageEngine.getMessageEmbed(guild, event.getUser(), modmailCommandHandler, "closeDeniedAdmin")).setComponents().queue();
				}
			}			
		}
	}
	
//	Tool methods
//	TODO Move to Join2CreateChannelData.class
	private void managej2cjoin(Guild guild, Member member, AudioChannel audioChannel) {
		JSONObject channelConfig = null;
		try {
			channelConfig = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(audioChannel.getId());
		} catch (JSONException e) {return;}
		if (channelConfig != null) {
			audioChannel.getPermissionContainer().upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VOICE_SPEAK).queue();
			Collection<Permission> defperms = new ArrayList<Permission>();
			defperms.add(Permission.VIEW_CHANNEL);
			defperms.add(Permission.VOICE_SPEAK);
			Collection<Permission> perms = new ArrayList<Permission>();
			if (channelConfig.getBoolean("configurable")) {
				perms.add(Permission.MANAGE_CHANNEL);
				perms.add(Permission.MANAGE_PERMISSIONS);
				perms.add(Permission.CREATE_INSTANT_INVITE);
				perms.add(Permission.VOICE_MUTE_OTHERS);
			}
			Category ctg = null;
			try {
				ICategorizableChannel temp = (ICategorizableChannel) audioChannel;
				ctg = temp.getParentCategory();
			} catch (ClassCastException ex) {};
			JSONObject createdChannels = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("createdchannels");
			try {
				createdChannels.getJSONObject(audioChannel.getId());
			} catch (JSONException ex) {
				createdChannels.put(audioChannel.getId(), new JSONObject());
			}
			int index = createdChannels.getJSONObject(audioChannel.getId()).keySet().size() + 1;
			String name = channelConfig.getString("name")
			    .replace("{member}", member.getEffectiveName())
			    .replace("{number}", String.valueOf(index));
			VoiceChannel nc = guild.createVoiceChannel(name, ctg).complete();
			nc.upsertPermissionOverride(guild.getPublicRole()).setAllowed(defperms).complete();
			nc.upsertPermissionOverride(member).setAllowed(perms).complete();
			if (channelConfig.getInt("limit") > 0) {
				nc.getManager().setUserLimit(channelConfig.getInt("limit")).queue();
			}
			guild.moveVoiceMember(member, nc).queue();
			createdChannels.getJSONObject(audioChannel.getId()).put(nc.getId(), new JSONArray().put(member.getUser().getIdLong()).put(index));
			//Update GUI information
            GUI.INSTANCE.increaseJ2CCounter();
		}
	}
	
	private void managej2cleave(Guild guild, User user, AudioChannel audioChannel) {
		int conmemb = audioChannel.getMembers().size();
		if (conmemb == 1) {
			if (audioChannel.getMembers().get(0).equals(guild.getSelfMember())) {
				Toolbox.stopMusicAndLeaveOn(guild);
				conmemb--;
			}
		}
		JSONObject createdchannels = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("createdchannels");
		List<String> parentChannels = new ArrayList<>();
		parentChannels.addAll(createdchannels.keySet());
		for (int i = 0; i < parentChannels.size(); i++) {
			try {
				JSONObject parentChannelData = createdchannels.getJSONObject(parentChannels.get(i));
				JSONArray channelData = parentChannelData.getJSONArray(audioChannel.getId());
				JSONObject parentChannelConfig = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("join2createchannels").getJSONObject(parentChannels.get(i));
				long ownerID = channelData.getLong(0);
				if (conmemb == 0) {
					audioChannel.delete().queue();
					parentChannelData.remove(audioChannel.getId());
					int index = channelData.getInt(1);
					//Update index numbers
					List<String> subChannels = new ArrayList<>();
					subChannels.addAll(parentChannelData.keySet());
					for (int a = 0; a < subChannels.size(); a++) {
						JSONArray subChannelData = parentChannelData.getJSONArray(subChannels.get(a));
						VoiceChannel target = guild.getVoiceChannelById(subChannels.get(a));
						int currentIndex = subChannelData.getInt(1);
						Member owner = guild.retrieveMemberById(subChannelData.getLong(0)).complete();
						String namePattern = parentChannelConfig.getString("name");
						String name = namePattern.replace("{member}",owner.getEffectiveName())
								.replace("{number}", String.valueOf(currentIndex));
						String newName = namePattern .replace("{member}", owner.getEffectiveName())
								.replace("{number}", String.valueOf(currentIndex - 1));
						if (subChannelData.getInt(1) > index
								&& namePattern.contains("{number}")
								&& target.getName().equals(name)) {
							target.getManager().setName(newName).queue();
							subChannelData.put(1, currentIndex - 1);
						}
					}
					//Update GUI information
                    GUI.INSTANCE.decreaseJ2CCounter();
				} else {
					if (ownerID == user.getIdLong()) {
						Collection<Permission> perms = new ArrayList<Permission>();
						if (parentChannelConfig.getBoolean("configurable")) {
							perms.add(Permission.MANAGE_CHANNEL);
							perms.add(Permission.MANAGE_PERMISSIONS);
							perms.add(Permission.CREATE_INSTANT_INVITE);
							perms.add(Permission.VOICE_MUTE_OTHERS);
						}
						Member newowner =  audioChannel.getMembers().get(0);
                        channelData.put(0, newowner.getIdLong());
						String name = audioChannel.getName().replace(guild.getMember(user).getEffectiveName(), newowner.getEffectiveName());
						audioChannel.getManager().setName(name).queue(sc -> {}, er -> {});
						audioChannel.getPermissionContainer().upsertPermissionOverride(newowner).setAllowed(perms).queue(sc -> {}, er -> {});
						audioChannel.getPermissionContainer().getPermissionOverride(guild.getMember(user)).delete().queue(sc -> {}, er -> {});
						audioChannel.getPermissionContainer().getManager().putPermissionOverride(newowner, perms, null).removePermissionOverride(guild.getMember(user)).setName(name).queue(sc -> {}, er -> {});
					}
				}
				i = parentChannels.size();
			} catch (JSONException ex) {}
		}
	}
}