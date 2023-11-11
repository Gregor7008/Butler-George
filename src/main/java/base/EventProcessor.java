package base;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import assets.data.MemberData;
import assets.data.single.AutoMessageData;
import assets.data.single.Join2CreateChannelData;
import assets.data.single.ModMailData;
import assets.data.single.ReactionRoleData;
import assets.functions.MessageContextEventHandler;
import assets.functions.SlashCommandEventHandler;
import assets.functions.UserContextEventHandler;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import engines.data.ConfigLoader;
import engines.functions.TrackScheduler;
import functions.context_menu_commands.MessageContextCommandList;
import functions.context_menu_commands.UserContextCommandList;
import functions.slash_commands.SlashCommandList;
import functions.slash_commands.support.Modmail;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
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
		Member member = event.getMember();
		if (member.getUser().isBot()) {
			return;
		}
	
		Guild guild = event.getGuild();
		if (event.getChannelJoined() != null) {
		    Join2CreateChannelData.manageJoin(guild, member, event.getChannelJoined());
		}
		
		if (event.getChannelLeft() != null) {
			AudioChannel audioChannel = event.getChannelLeft();
			int conmemb = audioChannel.getMembers().size();
			if (conmemb == 1) {
				if (audioChannel.getMembers().get(0).equals(guild.getSelfMember())) {
					TrackScheduler.stopMusicAndLeaveOn(guild);
					conmemb--;
				}
			}
			Join2CreateChannelData.manageLeave(guild, member, audioChannel);
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
}