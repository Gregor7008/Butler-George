package base.assets;

import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class CustomMessageEmbed extends MessageEmbed {


	public CustomMessageEmbed(MessageEmbed embed) {
		super(embed.getUrl(), embed.getTitle(), embed.getDescription(), embed.getType(), embed.getTimestamp(), embed.getColorRaw(), embed.getThumbnail(), embed.getSiteProvider(), embed.getAuthor(), embed.getVideoInfo(), embed.getFooter(), embed.getImage(),
			  embed.getFields());
	}
	
	public CustomMessageEmbed replaceTitle(String target, String replacement) {
		this.setTitle(this.getTitle().replace(target, replacement));
		return this;
	}
	
	public CustomMessageEmbed replaceDescription(String target, String replacement) {
		this.setDescription(this.getDescription().replace(target, replacement));
		return this;
	}
	
	public CustomMessageEmbed setTitle(String title) {
		this.setTitle(title);
		return this;
	}
	
	public CustomMessageEmbed setDescription(String description) {
		this.setDescription(description);
		return this;
	}
	
	public CustomMessageEmbed setFooter(String replacement, @Nullable String iconURL) {
		this.setFooter(replacement, iconURL);
		return this;
	}
	
	public CustomMessageEmbed setAuthor(Member member) {
		this.setAuthor(member);
		return this;
	}
	
	public CustomMessageEmbed setThumbnail(String url) {
		this.setThumbnail(url);
		return this;
	}
	@Deprecated
	public MessageEmbed convert() {
		return this;
	}
}