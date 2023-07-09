package assets.data.single;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import assets.data.DataTools;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class PenaltyData {

    private final Guild guild;
    private final PenaltyType type;
    private final int warning_limit;
    private Duration opt_duration;
    private List<Role> opt_roles = new ArrayList<>();
    private boolean remove_role, reset_experience, reset_level = false;
    
	public PenaltyData(Guild guild, JSONObject data) {
	    this.guild = guild;
	    this.type = PenaltyType.valueOf(data.getString(Key.TYPE));
	    this.warning_limit = data.getInt(Key.WARNING_LIMIT);
	    this.instanciateFromJSON(data);
	}
	
	public PenaltyData(Guild guild, PenaltyType type, int warning_limit) {
	    this.guild = guild;
	    this.type = type;
	    this.warning_limit = warning_limit;
	}

    public PenaltyData instanciateFromJSON(JSONObject data) {
        try {
            this.opt_duration = ConfigManager.convertStringToDuration(data.getString(Key.DURATION));
        } catch (JSONException e) {}
        
        this.opt_roles = DataTools.getRolesFromArrayKeys(guild, data, Key.ROLES, null);
        
        this.remove_role = data.getBoolean(Key.REMOVE_ROLE);
        this.reset_experience = data.getBoolean(Key.RESET_EXPERIENCE);
        this.reset_level = data.getBoolean(Key.RESET_LEVEL);
        
        return this;
    }

    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        if (this.opt_duration != null) {
            compiledData.put(Key.DURATION, ConfigManager.convertDurationToString(this.opt_duration));
        }
        
        if (this.opt_roles != null && !this.opt_roles.isEmpty()) {
            compiledData.put(Key.ROLES, 
                    new JSONArray(opt_roles.stream().map(role -> {return role.getIdLong();}).toList()));
        }
        
        if (this.opt_roles.isEmpty() && this.remove_role) {
            this.remove_role = false;
        }
        
        compiledData.put(Key.WARNING_LIMIT, warning_limit);
        compiledData.put(Key.TYPE, type.toString());
        compiledData.put(Key.REMOVE_ROLE, remove_role);
        compiledData.put(Key.RESET_EXPERIENCE, reset_experience);
        compiledData.put(Key.RESET_LEVEL, reset_level);
        
        return compiledData;
    }
    
    public Guild getGuild() {
        return this.guild;
    }
    
    public PenaltyType getType() {
        return this.type;
    }
    
    public Duration getDuration() {
        return this.opt_duration;
    }
    
    public PenaltyData setDuration(Duration duration) {
        this.opt_duration = duration;
        return this;
    }
    
    public List<Role> getRoles() {
        return this.opt_roles;
    }

    public PenaltyData setRoles(List<Role> roles) {
        DataTools.setList(this.opt_roles, roles);
        return this;
    }
    
    public PenaltyData addRoles(Role... roles) {
        DataTools.addToList(this.opt_roles, roles);
        return this;
    }
    
    public PenaltyData removeRoles(int... indices) {
        DataTools.removeIndiciesFromList(this.opt_roles, indices);
        return this;
    }
    
    public PenaltyData removeRolesByRole(Role... roles) {
        DataTools.removeValuesFromList(this.opt_roles, roles);
        return this;
    }
    
    public boolean includesExperienceReset() {
        return this.reset_experience;
    }
    
    public PenaltyData setExperienceReset(boolean reset_experience) {
        this.reset_experience = reset_experience;
        return this;
    }
    
    public boolean includesLevelReset() {
        return this.reset_level;
    }
    
    public PenaltyData setLevelReset(boolean reset_level) {
        this.reset_level = reset_level;
        return this;
    }
    
    public boolean includesRoleRemoval() {
        return this.remove_role;
    }
    
    public PenaltyData setRoleRemoval(boolean remove_role) {
        this.remove_role = remove_role;
        return this;
    }

    public int getWarningLimit() {
        return this.warning_limit;
    }
    
    private static abstract class Key {
        public static final String TYPE = "type";
        public static final String DURATION = "duration";
        public static final String ROLES = "roles";
        public static final String REMOVE_ROLE = "remove_role";
        public static final String RESET_EXPERIENCE = "reset_experience";
        public static final String RESET_LEVEL = "reset_level";
        public static final String WARNING_LIMIT = "warning_limit";
    }

    public static enum PenaltyType {
        REMOVE_ROLE,
        TEMPORARY_MUTE,
        PERMANENT_MUTE,
        KICK,
        TEMPORARY_BAN,
        PERMANENT_BAN;
    }
}