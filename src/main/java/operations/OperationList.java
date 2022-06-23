package operations;

import java.util.concurrent.ConcurrentHashMap;

import components.operations.OperationEventHandler;
import operations.administration.AutoRoles;
import operations.administration.BotAutoRoles;
import operations.administration.CustomChannelRoles;
import operations.administration.DefaultAccessRoles;
import operations.administration.Goodbye;
import operations.administration.Join2Create;
import operations.administration.LevelChannel;
import operations.administration.LevelReward;
import operations.administration.Penalty;
import operations.administration.ReactionRole;
import operations.administration.ReportChannel;
import operations.administration.SuggestionChannel;
import operations.administration.SupportTalk;
import operations.administration.Welcome;
import operations.moderation.Mute;
import operations.moderation.TempBan;
import operations.moderation.TempMute;
import operations.moderation.Unmute;

public class OperationList {
	
	public ConcurrentHashMap<String, OperationEventHandler> operations = new ConcurrentHashMap<>();

	public OperationList() {
		//Administration
		this.operations.put("AutoRole", new AutoRoles());
		this.operations.put("BotAutoRole", new BotAutoRoles());
		this.operations.put("CustomChannelRoles", new CustomChannelRoles());
		this.operations.put("DefaultAccessRoles", new DefaultAccessRoles());
		this.operations.put("Goodbye", new Goodbye());
		this.operations.put("Join2Create", new Join2Create());
		this.operations.put("LevelChannel", new LevelChannel());
		this.operations.put("LevelRewards", new LevelReward());
		this.operations.put("Penalty", new Penalty());
		this.operations.put("ReactionRole", new ReactionRole());
		this.operations.put("ReportChannel", new ReportChannel());
		this.operations.put("SuggestionChannel", new SuggestionChannel());
		this.operations.put("SupportTalk", new SupportTalk());
		this.operations.put("Welcome", new Welcome());
		//Moderation
		this.operations.put("Mute", new Mute());
		this.operations.put("TempBan", new TempBan());
		this.operations.put("TempMute", new TempMute());
		this.operations.put("Unmute", new Unmute());
	}
}