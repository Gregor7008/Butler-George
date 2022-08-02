package configuration_options.assets;

import javax.annotation.Nullable;

import base.assets.CustomMessageEmbed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;

public class ConfigurationEvent {
	
	private Guild guild = null;
	private User user = null;
	private Member member = null;
	private Message message = null;
	private MessageChannel channel = null;
	private GenericComponentInteractionCreateEvent event = null;
	private ConfigurationSubOptionData subOperation = null;

	public ConfigurationEvent(Guild guild, User user, GenericComponentInteractionCreateEvent event, @Nullable ConfigurationSubOptionData subOperation) {
		this.guild = guild;
		this.user = user;
		this.member = guild.getMember(user);
		this.event = event;
		this.subOperation = subOperation;
	}
	
	public ConfigurationEvent(Member member, GenericComponentInteractionCreateEvent event, @Nullable ConfigurationSubOptionData subOperation) {
		this.guild = member.getGuild();
		this.user = member.getUser();
		this.member = member;
		this.event = event;
		this.subOperation = subOperation;
	}
	
	public ConfigurationEvent setMessage(Message message) {
		this.message = message;
		return this;
	}
	
	public Guild getGuild() {
		return this.guild;
	}
	
	public User getUser() {
		return this.user;
	}
	
	public Member getMember() {
		return this.member;
	}
	
	public GenericComponentInteractionCreateEvent getSource() {
		return this.event;
	}
	
	public MessageChannel getChannel() {
		return this.channel;
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	public String getSubOperation() {
		return this.subOperation.getName();
	}
	
	public MessageEditCallbackAction replyEmbeds(CustomMessageEmbed embed) {
		return this.replyEmbeds(embed.convert());
	}
	
	public MessageEditCallbackAction replyEmbeds(MessageEmbed embed) {
		channel = event.getChannel();
		message = event.getMessage();
		return event.editMessageEmbeds(embed);
	}
}