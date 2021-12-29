package commands.utilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import components.utilities.LevelEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Level implements Command {
	
	private Guild guild;

	@Override
	public void perform(SlashCommandEvent event) {
		guild = event.getGuild();
		User user = event.getUser();
		event.deferReply(true);
		try {
			user = event.getOption("member").getAsUser();
		} catch (IllegalStateException | NullPointerException e) {}
		if (guild.getMember(user).getEffectiveName().equals(guild.getSelfMember().getEffectiveName())) {
			event.reply("You think you're funny or what?").queue();
			return;
		}
		File finalimage = this.renderLevelcard(user);
        event.reply("").addFile(finalimage).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("level", "Check your current level or the one of another user!")
											  .addOptions(new OptionData(OptionType.USER, "member", "Mention another user (optional)").setRequired(false));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/level:help");
	}
	
	private int calculateProgress(int level, int nedxp, String curxp) {
		int progress;
		int xpforlast = LevelEngine.getInstance().xpneededforlevel(level - 1);
		if (level != 0) {
			double temp1 = Double.valueOf(curxp) - xpforlast;
			double temp2 = (double) nedxp - xpforlast;
			double temp3 = temp1 / temp2 * 100;
			progress = (int) temp3;
		} else {
			if (Integer.valueOf(curxp) != 0) {
				progress = Integer.parseInt(curxp);
			} else {
				progress = 1;
			}
		}
		return progress;
	}
	
	public File renderLevelcard(User iuser) {
		String levelbackground = Configloader.INSTANCE.getUserConfig(guild, iuser, "levelbackground");
		int level = Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, iuser, "level"));
		String curxp = Configloader.INSTANCE.getUserConfig(guild, iuser, "expe");
		int nedxp = LevelEngine.getInstance().xpneededforlevel(Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, iuser, "level")));
		int progress = this.calculateProgress(level, nedxp, curxp);
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(Bot.environment + "/levelcards/" + levelbackground + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		BufferedImage avatar = null;
		File avfile = new File(Bot.environment + "/levelcards/cache/avatar.png");
		try {
			URL url = new URL(iuser.getAvatarUrl());
			FileUtils.copyURLToFile(url, avfile);
			avatar = ImageIO.read(avfile);
		} catch (Exception e) {
			e.printStackTrace();}
		
		//create editable Image
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//write Level
		g2d.setFont(new Font("Calibri", Font.PLAIN, 65));
		g2d.setColor(Color.decode("#5773c9"));
        g2d.drawString(String.valueOf(level), 813, 95);
		//write XP
        g2d.setFont(new Font("Calibri", Font.PLAIN, 30));
        g2d.setColor(Color.WHITE);
        String temp1 = curxp + "\s/\s" + String.valueOf(nedxp);
        g2d.drawString(temp1, 820 - g2d.getFontMetrics().stringWidth(temp1), 170);
		//write iusername
        g2d.setFont(new Font("Calibri", Font.PLAIN, 50));
        g2d.setColor(Color.WHITE);
        g2d.drawString(guild.getMember(iuser).getEffectiveName(), 293, 170);
		//draw Icon
		g2d.drawImage(this.makeRoundedCorner(avatar, avatar.getWidth()), 70, 50, 200, 200, null);
		//draw Progressbar
        BufferedImage progressbar = new BufferedImage(progress * 6, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pb = progressbar.createGraphics();
        pb.setColor(Color.decode("#5773c9"));
        pb.fillRect(0, 0, progress * 6, 40);
        pb.dispose();
        g2d.drawImage(this.makeRoundedCorner(progressbar, 30), 290, 185, null);
		//export the image and respond to the event
		g2d.dispose();
		File finalimage = new File(Bot.environment + "/levelcards/cache/temp.png");
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
}