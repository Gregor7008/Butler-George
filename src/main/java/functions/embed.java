package functions;

import main.Answer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class embed {
	
	String Title, Description, Author, AvatarURL, TNailURL, ImageURL, FooterText;
	String[] FTitle = new String[4];
	String[] FText = new String[4];
	TextChannel channel;
	boolean ILM;
	
	public embed(GuildMessageReceivedEvent event, String object, Member member) {
		if (!object.equals("none")) {
		Author = member.getEffectiveName();
		AvatarURL = member.getUser().getAvatarUrl();
		channel = event.getChannel();
		try {
			this.messagesplit(object);
		} catch (Exception e) {
			new Answer("Error", ":exclamation: | Something seems to have failed... \n Error code: 002", event, true);
		}
		this.buildMessage();
		} else {
			new Answer("?!", ":exclamation: | HOW should I know what to send? \n Error code: 003", event, false);
		}
	}
	
	private void messagesplit(String message) {
		String[] raw = message.split("\\$");
		for (int i = 0; raw.length-1 >= i; i++) {
				if (raw[i].contains("-t")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					Title = temp1[1];
			} else { 
				if (raw[i].contains("-d")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					Description = temp1[1];
			} else { 
				if (raw[i].contains("-i")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					ImageURL = temp1[1];
			} else {
				if (raw[i].contains("-o")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					FooterText = temp1[1];
			} else {
				if (raw[i].contains("-n")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					TNailURL = temp1[1];
			} else {
				if (raw[i].contains("-m")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					if (temp1[1].equalsIgnoreCase("true")) {
						ILM = true;
					} else {
						ILM = false;
					}
			} else {
				if (raw[i].contains("-f")) {
					String[] temp1 = raw[i].split("\\s+", 2);
					String[] temp2 = temp1[1].split(";"+"\\s+");
					for (int o = 0; temp2.length-1 >= o; o++) {
						String[] temp3 = temp2[o].split("/");
						FTitle[o] = temp3[0];
						FText[o] = temp3[1];
			}}}}}}}}
		}
	}
	
	private void buildMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(Title);
		eb.setColor(56575);
		eb.setDescription(Description);
		eb.setAuthor(Author, null, AvatarURL);
		eb.setFooter(FooterText);
		eb.setImage(ImageURL);
		eb.setThumbnail(TNailURL);
		eb.addField(FTitle[0],FText[0], ILM);
		if (FTitle[1]!=null) {
			eb.addField(FTitle[1],FText[1], ILM);
		}
		if (FTitle[2]!=null) {
			eb.addField(FTitle[2],FText[2], ILM);
		}
		if (FTitle[3]!=null) {
			eb.addField(FTitle[3],FText[3], ILM);
		}
		channel.sendMessage(eb.build()).queue();
	}
}
