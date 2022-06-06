package components.base.assets;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CustomMessageEmbed {

	private EmbedBuilder eb = new EmbedBuilder();
	private MessageEmbed em;
	
	public CustomMessageEmbed(MessageEmbed embed) {
		em = embed;
		eb.copyFrom(embed);
	}
	
	public CustomMessageEmbed replaceTitle(String target, String replacement) {
		eb.setTitle(em.getTitle().replace(target, replacement));
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed replaceDescription(String target, String replacement) {
		eb.setDescription(em.getDescription().replace(target, replacement));
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setTitle(String title) {
		eb.setTitle(title);
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setDescription(String description) {
		eb.setDescription(description);
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setFooter(String replacement,@Nullable String iconURL) {
		eb.setFooter(replacement, iconURL);
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setAuthor(Member member) {
		eb.setAuthor(member.getEffectiveName(), null, member.getAvatarUrl());
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setThumbnail(String url) {
		eb.setThumbnail(url);
		return new CustomMessageEmbed(eb.build());
	}
	
	public MessageEmbed convert() {
		return eb.build();
	}
}