package components.base;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class AnswerEditor {

	private EmbedBuilder eb = new EmbedBuilder();
	private MessageEmbed em;
	
	public AnswerEditor(MessageEmbed embed) {
		em = embed;
		eb.copyFrom(embed);
	}
	
	public AnswerEditor replaceTitle(char target, char replacement) {
		eb.setTitle(em.getTitle().replace(target, replacement));
		return new AnswerEditor(eb.build());
	}
	
	public AnswerEditor replaceDescription(char target, char replacement) {
		eb.setDescription(em.getDescription().replace(target, replacement));
		return new AnswerEditor(eb.build());
	}
	
	public AnswerEditor setFooter(String replacement,@Nullable String iconURL) {
		eb.setFooter(replacement, iconURL);
		return new AnswerEditor(eb.build());
	}
	
	public AnswerEditor setAuthor(Member member) {
		eb.setAuthor(member.getEffectiveName(), null, member.getAvatarUrl());
		return new AnswerEditor(eb.build());
	}
	
	public AnswerEditor setThumbnail(String url) {
		eb.setThumbnail(url);
		return new AnswerEditor(eb.build());
	}
	
	public MessageEmbed convert() {
		return eb.build();
	}
}