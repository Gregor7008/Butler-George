package engines.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoQueryException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import assets.base.exceptions.EntityNotFoundException;
import assets.data.GuildData;
import assets.data.MemberData;
import assets.data.UserData;
import assets.data.single.SystemData;
import assets.logging.Logger;
import base.Bot;
import base.GUI;
import engines.base.CentralTimer;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConfigLoader {

    public static DateTimeFormatter DATA_TIME_SAVE_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss - dd.MM.yyyy | O");
    public static int PUSH_CYCLE_PERIOD = 15;

    private static ConfigLoader INSTANCE;
    private static Logger LOG = ConsoleEngine.getLogger(ConfigLoader.class);

    private SystemData systemData;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> userConfigs;
    private MongoCollection<Document> guildConfigs;
    private ConcurrentHashMap<Long, UserData> userConfigCache = new ConcurrentHashMap<Long, UserData>();
    private ConcurrentHashMap<Long, GuildData> guildConfigCache = new ConcurrentHashMap<Long, GuildData>();

    public static ConfigLoader get() {
        if (INSTANCE != null) {
            return INSTANCE;
        } else {
            LOG.debug("Instance not found, try ConfigLoader.connect(String licenseKey) first!");
            return null;
        }
    }

    public static boolean connect(String licenseKey) {
        InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("misc/secrets.json");
        String connectionUrl = new JSONObject(new JSONTokener(is)).getString("auth_server_url");
        try {
            is.close();
        } catch (IOException e) {}
        MongoClientSettings setting = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(3, TimeUnit.SECONDS);
                    builder.readTimeout(500, TimeUnit.MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(3, TimeUnit.SECONDS))
                .applyConnectionString(new ConnectionString(connectionUrl))
                .build();
        try {
            MongoClient preCheckClient = MongoClients.create(setting);
            MongoCollection<Document> preCheckCollection = preCheckClient.getDatabase("system")
                    .getCollection("licenses");
            Document queryResult = preCheckCollection.find(new Document("license_key", licenseKey)).first();
            if (queryResult != null) {
                new ConfigLoader(new SystemData(new JSONObject(queryResult.toJson())));
            } else {
                LOG.error("License Key invalid, please try again!");
            }
        } catch (MongoSecurityException | MongoQueryException | MongoTimeoutException e) {
            return false;
        }
        return true;
    }

    private ConfigLoader(SystemData data) {
        this.systemData = data;
        String connectionUri = "mongodb://" + this.encodeToURL(data.getDatabaseUsername()) + ":"
                + this.encodeToURL(data.getDatabasePassword())
                + "@butlergeorge.ddns.net:17389/?authMechanism=SCRAM-SHA-256";
        MongoClientSettings setting = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(3, TimeUnit.SECONDS);
                    builder.readTimeout(500, TimeUnit.MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(3, TimeUnit.SECONDS))
                .applyConnectionString(new ConnectionString(connectionUri))
                .build();
        try {
            client = MongoClients.create(setting);
            database = client.getDatabase(data.getDatabaseName());
            try {
                userConfigs = database.getCollection("user");
            } catch (IllegalArgumentException e) {
                database.createCollection("user");
                userConfigs = database.getCollection("user");
            }
            try {
                guildConfigs = database.getCollection("guild");
            } catch (IllegalArgumentException e) {
                database.createCollection("guild");
                guildConfigs = database.getCollection("guild");
            }
            try {
                guildConfigs.find(new Document("id", 475974084937646080L)).first();
                CentralTimer.get().schedule(new Runnable() {
                    private int executions = 0;

                    @Override
                    public void run() {
                        if (executions > 1) {
                            pushCache();
                        }
                        executions++;
                    }
                }, TimeUnit.MINUTES, 0, TimeUnit.MINUTES, PUSH_CYCLE_PERIOD);
                INSTANCE = this;
            } catch (MongoSecurityException | MongoQueryException e) {
                client.close();
                LOG.error("Authentication values invalid, please contact support!");
            }
        } catch (MongoTimeoutException e) {
            LOG.error("Connection to server failed, please contact support!");
        }
    }

    private String encodeToURL(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

//  Config Cache
    public ConcurrentHashMap<Long, UserData> getUserCache() {
        return userConfigCache;
    }

    public ConcurrentHashMap<Long, GuildData> getGuildCache() {
        return guildConfigCache;
    }

    public void printCache() {
        LOG.info("---------| User-Cache |---------");
        userConfigCache.forEach((id, data) -> {
            LOG.info("-> " + Bot.getAPI().retrieveUserById(id).complete().getName());
        });
        if (userConfigCache.isEmpty()) {
            LOG.info("EMPTY");
        }
        LOG.info("---------| Guild-Cache |--------");
        guildConfigCache.forEach((id, data) -> {
            LOG.info("-> " + Bot.getAPI().getGuildById(id).getName());
        });
        if (guildConfigCache.isEmpty()) {
            LOG.info("EMPTY");
        }
    }

    public boolean pushCache() {
        try {
            boolean changes_made = false;
            if (!userConfigCache.isEmpty()) {
                userConfigCache.forEach((id, data) -> {
                    Document searchresult = userConfigs.find(new Document("id", Long.valueOf(id))).first();
                    if (searchresult != null) {
                        userConfigs.replaceOne(searchresult, Document.parse(data.compileToJSON().toString()));
                    } else {
                        userConfigs.insertOne(Document.parse(data.compileToJSON().toString()));
                    }
                });
                userConfigCache.clear();
                changes_made = true;
            }
            if (!guildConfigCache.isEmpty()) {
                guildConfigCache.forEach((id, data) -> {
                    Document searchresult = guildConfigs.find(new Document("id", Long.valueOf(id))).first();
                    if (searchresult != null) {
                        guildConfigs.replaceOne(searchresult, Document.parse(data.compileToJSON().toString()));
                    } else {
                        guildConfigs.insertOne(Document.parse(data.compileToJSON().toString()));
                    }
                });
                guildConfigCache.clear();
                changes_made = true;
            }
            if (changes_made) {
                GUI.INSTANCE.increasePushCounter();
            }
            return changes_made;
        } catch (Exception e) {
            LOG.error("Push failed!");
            return false;
        }
    }

//    Get Data
    public SystemData getSystemData() {
        return this.systemData;
    }

    public GuildData getGuildData(long guild_id) {
        GuildData guildData = guildConfigCache.get(guild_id);
        if (guildData == null) {
            Document doc = guildConfigs.find(new Document("id", guild_id)).first();
            if (doc != null) {
                guildData = new GuildData(new JSONObject(doc.toJson()));
            } else {
                try {
                    guildData = new GuildData(Bot.getAPI().getGuildById(guild_id));
                } catch (EntityNotFoundException e) {
                    LOG.debug(e.getMessage());
                }
            }
            guildConfigCache.put(guild_id, guildData);
        }
        return guildData;
    }

    public GuildData getGuildData(Guild guild) {
        return this.getGuildData(guild.getIdLong());
    }

    public UserData getUserData(long user_id) {
        UserData userData = userConfigCache.get(user_id);
        if (userData == null) {
            Document doc = userConfigs.find(new Document("id", user_id)).first();
            if (doc != null) {
                userData = new UserData(new JSONObject(doc.toJson()));
            } else {
                try {
                    userData = new UserData(Bot.getAPI().retrieveUserById(user_id).complete());
                } catch (EntityNotFoundException e) {
                    LOG.debug(e.getMessage());
                }
            }
            userConfigCache.put(user_id, userData);
        }
        return userData;
    }

    public UserData getUserData(User user) {
        return this.getUserData(user.getIdLong());
    }

    public MemberData getMemberData(Guild guild, User user) {
        return this.getUserData(user).getMemberData(guild);
    }
}