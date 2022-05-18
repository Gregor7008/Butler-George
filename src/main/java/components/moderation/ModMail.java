package components.moderation;

import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ModMail {
	
	public static final Guild guild = Bot.INSTANCE.jda.getGuildById("958763161362772069");

	public ModMail(MessageReceivedEvent event, boolean direction) {
//		User user = event.getAuthor();
//		if (user.isBot()) {
//			return;
//		}
//		if (guild == null) {
//			return;
//		}
//		if (direction) {
//			if (Configloader.INSTANCE.getMailConfig1(event.getChannel().getId()) != null) {
//				PrivateChannel pc = Bot.INSTANCE.jda.openPrivateChannelById(Configloader.INSTANCE.getMailConfig1(event.getChannel().getId())).complete();
//				this.resendMessage(pc, event.getMessage());
//			}
//			return;
//		}
//		boolean member = false;
//		try {
//			if (guild.retrieveMember(user).complete() != null) {
//				member = true;
//			}
//		} catch (ErrorResponseException e) {}
//		if (guild.retrieveBan(user).complete() == null && !member) {
//			event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(null, null, "/components/moderation/modmail:nosupport").convert()).queue();
//			return;
//		}
//		if (Configloader.INSTANCE.getMailConfig2(user.getId()) != null) {
//			TextChannel channel = guild.getTextChannelById(Configloader.INSTANCE.getMailConfig2(user.getId()));
//			this.resendMessage(channel, event.getMessage());
//			return;
//		}
//		if (!event.getMessage().getContentRaw().contains("anonym")) {
//			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "lastmail"));
//			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
//				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:success").convert()).queue();
//				Configloader.INSTANCE.setUserConfig(guild, user, "lastmail", OffsetDateTime.now().toString());
//				this.processMessage(event);
//			} else {
//				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
//				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:timelimit")
//						.replaceDescription("{timeleft}", String.valueOf(timeleft)).convert()).queue();
//			}
//		} else {
//			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "lastmail"));
//			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 1) {
//				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:successanonym").convert()).queue();
//				Configloader.INSTANCE.setUserConfig(guild, user, "lastmail", OffsetDateTime.now().toString());
//				this.processAnonymousMessage(event);
//			} else {
//				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
//				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:timelimit")
//						.replaceDescription("{timeleft}", String.valueOf(timeleft)).convert()).queue();
//			}
//		}
	}
	
//	private void processMessage(MessageReceivedEvent event) {
//		if (Configloader.INSTANCE.getGuildConfig(guild, "supportcategory").equals("")) {
//			Category ctg = guild.createCategory("----------📝 Tickets ------------").complete();
//			Configloader.INSTANCE.setGuildConfig(guild, "supportcategory", ctg.getId());
//		}
//		TextChannel nc = guild.createTextChannel(event.getAuthor().getName(), guild.getCategoryById(Configloader.INSTANCE.getGuildConfig(guild, "supportcategory"))).complete();
//		nc.sendMessage(event.getMessage()).queue();
//		nc.sendMessage(guild.getPublicRole().getAsMention());
//		nc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
//		Configloader.INSTANCE.setMailConfig(nc.getId(), event.getAuthor().getId());
//	}
//	
//	private void processAnonymousMessage(MessageReceivedEvent event) {
//		int rn = new Random().nextInt(100);
//		if (Configloader.INSTANCE.getGuildConfig(guild, "supportcategory").equals("")) {
//			Category ctg = guild.createCategory("----------📝 Tickets ------------").complete();
//			Configloader.INSTANCE.setGuildConfig(guild, "supportcategory", ctg.getId());
//		}
//		TextChannel nc = guild.createTextChannel(String.valueOf(rn), guild.getCategoryById(Configloader.INSTANCE.getGuildConfig(guild, "supportcategory"))).complete();
//		this.resendMessage(nc, event.getMessage());
//		nc.sendMessage(guild.getPublicRole().getAsMention());
//		nc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
//		Configloader.INSTANCE.setMailConfig(nc.getId(), event.getAuthor().getId());
//	}
	
//	private void resendMessage(MessageChannel channel, Message message) {
//		List<Attachment> attachements = message.getAttachments();
//		List<File> files = new ArrayList<>();
//		for (int i = 0; i < attachements.size(); i++) {
//			File file = new File(Bot.environment + "/cache/" + attachements.get(i).getFileName());
//			Boolean deleted = true;
//			if (file.exists()) {
//				deleted = file.delete();
//			}
//			if (deleted) {
//				try {
//					attachements.get(i).downloadToFile(file).get();
//				} catch (InterruptedException | ExecutionException e) {}
//				files.add(file);
//			}
//		}
//		channel.sendMessage(message).queue();
//		for (int i = 0; i < files.size(); i++) {
//			File file = files.get(i);
//			channel.sendFile(file).queue(e -> file.delete());
//		}
//	}
}