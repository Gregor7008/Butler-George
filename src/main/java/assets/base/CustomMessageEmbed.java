package assets.base;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CustomMessageEmbed extends MessageEmbed {

	EmbedBuilder eb = new EmbedBuilder();

	public CustomMessageEmbed(MessageEmbed embed) {
		super(embed.getUrl(), embed.getTitle(), embed.getDescription(), embed.getType(), embed.getTimestamp(),
				embed.getColorRaw(), embed.getThumbnail(), embed.getSiteProvider(), embed.getAuthor(),
				embed.getVideoInfo(), embed.getFooter(), embed.getImage(), embed.getFields());
		eb.copyFrom(embed);
	}
	
	public CustomMessageEmbed replaceTitle(String target, String replacement) {
		eb.setTitle(this.getTitle().replace(target, replacement));
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed replaceDescription(String target, String replacement) {
		eb.setDescription(this.getDescription().replace(target, replacement));
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
	
	public CustomMessageEmbed setFooter(String replacement, @Nullable String iconURL) {
		eb.setFooter(replacement, iconURL);
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setAuthor(Member member) {
		eb.setAuthor(member.getEffectiveName(), member.getAvatarUrl());
		return new CustomMessageEmbed(eb.build());
	}
	
	public CustomMessageEmbed setThumbnail(String url) {
		eb.setThumbnail(url);
		return new CustomMessageEmbed(eb.build());
	}
}