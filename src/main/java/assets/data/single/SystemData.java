package assets.data.single;

import org.json.JSONObject;

public class SystemData {
    
    private static String LICENSE_KEY, TOKEN, BOT_NAME, BOT_OWNER_NAME, USERNAME, PASSWORD, DATABASE;
    private static long BOT_ID, BOT_OWNER_ID, HOME_SERVER_ID;

    public SystemData(JSONObject data) {
        LICENSE_KEY = data.getString(Key.LICENSE_KEY);
        TOKEN = data.getString(Key.TOKEN);
        BOT_NAME = data.getString(Key.BOT_NAME);
        BOT_OWNER_NAME = data.getString(Key.BOT_OWNER_NAME);
        USERNAME = data.getString(Key.USERNAME);
        PASSWORD = data.getString(Key.PASSWORD);
        DATABASE = data.getString(Key.DATABASE);
        
        BOT_ID = data.getLong(Key.BOT_ID);
        BOT_OWNER_ID = data.getLong(Key.BOT_OWNER_ID);
        HOME_SERVER_ID = data.getLong(Key.HOME_SERVER_ID);
    }
    
    public String getLicenseKey() {
        return LICENSE_KEY;
    }
    
    public String getBotToken() {
        return TOKEN;
    }
    
    public String getBotName() {
        return BOT_NAME;
    }
    
    public long getBotId() {
        return BOT_ID;
    }
    
    public String getBotOwnerName() {
        return BOT_OWNER_NAME;
    }
    
    public long getBotOwnerId() {
        return BOT_OWNER_ID;
    }
    
    public long getHomeServerId() {
        return HOME_SERVER_ID;
    }
    
    public String getDatabaseUsername() {
        return USERNAME;
    }
    
    public String getDatabasePassword() {
        return PASSWORD;
    }
    
    public String getDatabaseName() {
        return DATABASE;
    }
    
    private static abstract class Key {
        public static final String LICENSE_KEY = "license_key";
        public static final String TOKEN = "token";
        public static final String BOT_NAME = "bot_name";
        public static final String BOT_ID = "bot_id";
        public static final String BOT_OWNER_NAME = "bot_owner_name";
        public static final String BOT_OWNER_ID = "bot_owner_id";
        public static final String HOME_SERVER_ID = "home_id";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String DATABASE = "database";
    }
}