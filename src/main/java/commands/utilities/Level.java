package commands.utilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import components.base.LanguageEngine;
import components.commands.Command;
import components.commands.utilities.LevelEngine;
import components.base.ConfigLoader;
import components.base.ConsoleEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Level implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getOption("user") != null) {
			user = event.getOption("user").getAsUser();
		}
		if (guild.getMember(user).equals(guild.getSelfMember())) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/eastereggs:3").convert()).queue(r -> r.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		event.deferReply().queue();
		File finalimage = this.renderLevelcard(user, guild);
        if (finalimage == null) {
        	event.getHook().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/general:fatal").convert()).queue();
        } else {
        	event.getHook().sendMessage("").addFile(finalimage).queue();
        }
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("level", "Check your current level or the one of another user!")
											  .addOptions(new OptionData(OptionType.USER, "user", "Mention another user (optional)").setRequired(false));
		return command;
	}
	
	public File renderLevelcard(User iuser, Guild guild) {
		String levelbackground = String.valueOf(ConfigLoader.getMemberConfig(guild, iuser).getInt("levelbackground"));
		int level = ConfigLoader.getMemberConfig(guild, iuser).getInt("level");
		String curxp = String.valueOf(ConfigLoader.getMemberConfig(guild, iuser).getInt("experience"));
		int nedxp = LevelEngine.getInstance().xpneededforlevel(level);
		int progress = this.calculateProgress(level, nedxp, curxp);
		BufferedImage image = null;
		try {
			image = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("levelcards/" + levelbackground + ".png"));
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
			return null;
		}
		BufferedImage avatar = null;
		File avfile = null;
		try {
			avfile = File.createTempFile("avatar", ".png");
		} catch (IOException e) {}
		try {
			URL url = new URL(iuser.getAvatarUrl());
			FileUtils.copyURLToFile(url, avfile);
			avatar = ImageIO.read(avfile);
		} catch (Exception e) {
			e.printStackTrace();}
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getClassLoader().getResourceAsStream("levelcards/font.ttf"));
		} catch (FontFormatException | IOException e) {
			ConsoleEngine.out.error(this, "Couldn't load font!");
			return null;
		}
		
		//create editable Image
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//write Level
		g2d.setFont(font.deriveFont(60f));
		g2d.setColor(Color.decode("#5773c9"));
        g2d.drawString(String.valueOf(level), 813, 95);
		//write XP
        g2d.setFont(font.deriveFont(30f));
        g2d.setColor(Color.WHITE);
        String temp1 = curxp + "\s/\s" + String.valueOf(nedxp);
        g2d.drawString(temp1, 820 - g2d.getFontMetrics().stringWidth(temp1), 170);
		//write iusername
        g2d.setFont(font.deriveFont(55f));
        g2d.setColor(Color.WHITE);
        g2d.drawString(guild.getMember(iuser).getEffectiveName(), 293, 170);
		//draw Icon
		g2d.drawImage(this.makeRoundedCorner(avatar, avatar.getWidth()), 70, 50, 200, 200, null);
		//draw Progressbar
        if (progress > 0) {
        	BufferedImage progressbar = new BufferedImage(progress * 6, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D pb = progressbar.createGraphics();
            pb.setColor(Color.decode("#5773c9"));
            pb.fillRect(0, 0, progress * 6, 40);
            pb.dispose();
            g2d.drawImage(this.makeRoundedCorner(progressbar, 30), 290, 185, null);
        }
		//export the image and respond to the event
		g2d.dispose();
		File finalimage = null;
		try {
			finalimage = File.createTempFile("finalcard", ".png");
		} catch (IOException e) {}
		try {
			ImageIO.write(image, "png", finalimage);
		}catch (IOException e) {
			return null;
		}
		return finalimage;
	}
	
	private BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();
	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(image, 0, 0, null);
	    g2.dispose();
	    
	    return output;
	}
	
	private int calculateProgress(int level, int nedxp, String curxp) {
		int progress;
		int xpforlast = LevelEngine.getInstance().xpneededforlevel(level - 1);
		double temp1 = Double.valueOf(curxp) - xpforlast;
		double temp2 = (double) nedxp - xpforlast;
		double temp3 = temp1 / temp2 * 100;
		progress = (int) temp3;
		return progress;
	}
}