package assets.base;

import com.mongodb.lang.Nullable;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MutableMessageEmbed extends MessageEmbed {

	EmbedBuilder eb = new EmbedBuilder();

	public MutableMessageEmbed(MessageEmbed embed) {
		super(embed.getUrl(), embed.getTitle(), embed.getDescription(), embed.getType(), embed.getTimestamp(),
				embed.getColorRaw(), embed.getThumbnail(), embed.getSiteProvider(), embed.getAuthor(),
				embed.getVideoInfo(), embed.getFooter(), embed.getImage(), embed.getFields());
		eb.copyFrom(embed);
	}
	
	public MutableMessageEmbed replaceTitle(String target, String replacement) {
		eb.setTitle(this.getTitle().replace(target, replacement));
		return new MutableMessageEmbed(eb.build());
	}
	
	public MutableMessageEmbed replaceDescription(String target, String replacement) {
		eb.setDescription(this.getDescription().replace(target, replacement));
		return new MutableMessageEmbed(eb.build());
	}
	
	public MutableMessageEmbed setTitle(String title) {
		eb.setTitle(title);
		return new MutableMessageEmbed(eb.build());
	}
	
	public MutableMessageEmbed setDescription(String description) {
		eb.setDescription(description);
		return new MutableMessageEmbed(eb.build());
	}
	
	public MutableMessageEmbed setFooter(String replacement, @Nullable String iconURL) {
		eb.setFooter(replacement, iconURL);
		return new MutableMessageEmbed(eb.build());
	}
	
	public MutableMessageEmbed setAuthor(Member member) {
		eb.setAuthor(member.getEffectiveName(), member.getAvatarUrl());
		return new MutableMessageEmbed(eb.build());
	}
	
	public MutableMessageEmbed setThumbnail(String url) {
		eb.setThumbnail(url);
		return new MutableMessageEmbed(eb.build());
	}
}