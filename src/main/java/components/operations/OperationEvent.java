package components.operations;

import javax.annotation.Nullable;

import components.utilities.Replyable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class OperationEvent extends Replyable {
	
	private Guild guild = null;
	private User user = null;
	private Member member = null;
	private TextChannel channel = null;
	private SubOperationData subOperation = null;

	public OperationEvent(Guild guild, User user, Message message, @Nullable SubOperationData subOperation) {
		this.guild = guild;
		this.user = user;
		this.member = guild.getMember(user);
		this.message = message;
		this.channel = message.getTextChannel();
		this.subOperation = subOperation;
	}
	
	public OperationEvent(Member member, Message message, @Nullable SubOperationData subOperation) {
		this.guild = member.getGuild();
		this.user = member.getUser();
		this.member = member;
		this.message = message;
		this.channel = message.getTextChannel();
		this.subOperation = subOperation;
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
	
	public Message getMessage() {
		return this.message;
	}
	
	public TextChannel getChannel() {
		return this.channel;
	}
	
	public SubOperationData getSubOperation() {
		return this.subOperation;
	}
}