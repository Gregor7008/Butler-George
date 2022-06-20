package components.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import components.base.CustomMessageEmbed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;

public class Action extends IOptionHolder {
	
	private Guild guild = null;
	private User user = null;
	private Member member = null;
	private TextChannel channel = null;
	private SubAction subAction = null;

	public Action(Guild guild, User user, TextChannel channel, @Nullable SubAction subAction, @Nullable Object[] options) {
		this.guild = guild;
		this.user = user;
		this.member = guild.getMember(user);
		this.channel = channel;
		this.subAction = subAction;
		this.options = options;
	}
	
	public Action(Member member, TextChannel channel, @Nullable SubAction subAction, @Nullable Object[] options) {
		this.guild = member.getGuild();
		this.user = member.getUser();
		this.member = member;
		this.channel = channel;
		this.subAction = subAction;
		this.options = options;
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
	
	public TextChannel getTextChannel() {
		return this.channel;
	}
	
	public SubAction getSubAction() {
		return this.subAction;
	}
	
	public Object[] getOptions() {
		return this.options;
	}

	public Message reply(String message) {
		return channel.sendMessage(message).complete();
	}
	
	public Message replyEmbeds(MessageEmbed embed) {
		return channel.sendMessageEmbeds(embed).complete();
	}
	
	public Message replyEmbeds(CustomMessageEmbed embed) {
		return channel.sendMessageEmbeds(embed.convert()).complete();
	}
	
	public RestAction<Message> replyEmbedsRA(MessageEmbed embed) {
		return channel.sendMessageEmbeds(embed);
	}
	
	public RestAction<Message> replyEmbedsRA(CustomMessageEmbed embed) {
		return channel.sendMessageEmbeds(embed.convert());
	}
	
	public Message replyEmbeds(Collection<MessageEmbed> embeds) {
		return channel.sendMessageEmbeds(embeds).complete();
	}
	
	public Message replyCustomEmbeds(Collection<CustomMessageEmbed> embeds) {
		List<MessageEmbed> convertedEmbeds = new ArrayList<>();
		embeds.forEach(e -> convertedEmbeds.add(e.convert()));
		return channel.sendMessageEmbeds(convertedEmbeds).complete();
	}
}