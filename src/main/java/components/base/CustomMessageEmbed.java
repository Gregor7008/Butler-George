package components.base;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CustomMessageEmbed {

	private EmbedBuilder eb = new EmbedBuilder();
	
	public CustomMessageEmbed(MessageEmbed embed) {
		eb.copyFrom(embed);
	}
	
	public CustomMessageEmbed replaceTitle(String target, String replacement) {
		eb.setTitle(eb.build().getTitle().replace(target, replacement));
		return this;
	}
	
	public CustomMessageEmbed replaceDescription(String target, String replacement) {
		eb.setDescription(eb.build().getDescription().replace(target, replacement));
		return this;
	}
	
	public CustomMessageEmbed setTitle(String title) {
		eb.setTitle(title);
		return this;
	}
	
	public CustomMessageEmbed setDescription(String description) {
		eb.setDescription(description);
		return this;
	}
	
	public CustomMessageEmbed setFooter(String replacement, @Nullable String iconURL) {
		eb.setFooter(replacement, iconURL);
		return this;
	}
	
	public CustomMessageEmbed setAuthor(Member member) {
		eb.setAuthor(member.getEffectiveName(), null, member.getAvatarUrl());
		return this;
	}
	
	public CustomMessageEmbed setThumbnail(String url) {
		eb.setThumbnail(url);
		return this;
	}
	
	public MessageEmbed convert() {
		return eb.build();
	}
}