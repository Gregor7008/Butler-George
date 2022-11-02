package assets.functions;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public class ConfigurationEvent {
	
	private Guild guild = null;
	private User user = null;
	private Member member = null;
	private Message message = null;
	private MessageChannel channel = null;
	private GenericComponentInteractionCreateEvent event = null;
	private ConfigurationSubOptionData subOperation = null;
	
	public ConfigurationEvent(Member member, GenericComponentInteractionCreateEvent event, @Nullable ConfigurationSubOptionData subOperation) {
		this.guild = member.getGuild();
		this.user = member.getUser();
		this.message = event.getMessage();
		this.channel = this.message.getChannel();
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
}