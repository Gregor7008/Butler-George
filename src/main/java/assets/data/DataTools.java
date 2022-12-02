package assets.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public abstract class DataTools {
    
//  List and Map tools
    public static <T> void setList(List<T> target, List<T> replacement) {
        if (replacement == null) {
            target.clear();
        } else {
            target = new ArrayList<>(replacement);
            target.removeAll(Collections.singleton(null));
        }
    }
    
    @SafeVarargs
    public static <T> void addToList(List<T> target, T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                target.add(values[i]);
            }
        }
    }
    
    public static <T> void removeIndiciesFromList(List<T> target, int... indices) {
        for (int i = 0; i < indices.length; i++) {
            target.remove(indices[i]);
        }
    }
    
    @SafeVarargs
    public static <T> void removeValuesFromList(List<T> target, T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values != null) {
                target.remove(values[i]);
            }
        }
    }
    
    public static <K, V> void setMap(ConcurrentHashMap<K, V> target, ConcurrentHashMap<K, V> replacement) {
        if (replacement == null) {
            target.clear();
        } else {
            target = replacement;
        }
    }

    @SafeVarargs
    public static <K, V> void removeKeysFromMap(ConcurrentHashMap<K, V> target, K... keys) {
        for (int i = 0; i < keys.length; i++) {
            target.remove(keys[i]);
        }
    }
    
    @SafeVarargs
    public static <K, V> void removeValuesFromMap(ConcurrentHashMap<K, V> target, V... values) {
        List<K> keysToRemove = new ArrayList<>();
        List<V> valueList = new ArrayList<>();
        valueList.addAll(List.of(values));
        target.forEach((key, value) -> {
            if (valueList.contains(value)) {
                keysToRemove.add(key);
            }
        });
        for (int i = 0; i < keysToRemove.size(); i++) {
            target.remove(keysToRemove.get(i));
        }
    }
    
//  Data conversion tools
    public static List<Long> getIdsFromArrayKeys(JSONObject data, String primary, @Nullable String secondary) {
        try {
            JSONArray values = null;
            if (secondary == null) {
                values = data.getJSONArray(primary);
            } else {
                values = data.getJSONObject(primary).getJSONArray(secondary);
            }
            List<Long> values_list = new ArrayList<>();
            for (int i = 0; i < values.length(); i++) {
                values_list.add(values.getLong(i));
            }
            return values_list;
        } catch (JSONException e) {
            return null;
        }
    }
    
    public static List<Role> getRolesFromIds(Guild guild, List<Long> ids) {
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Role role = guild.getRoleById(ids.get(i));
            if (role != null) {
                roles.add(role);
            } else {
                ids.remove(i);
                i -= 1;
            }
        }
        return roles;
    }
    
    public static Long[] convertRoleArrayToIds(Role[] roles) {
        return DataTools.convertRoleListToIds(new ArrayList<>(List.of(roles)));
    }
    
    public static Long[] convertRoleListToIds(List<Role> roles) {
        ArrayList<Long> ids = new ArrayList<>();
        for (int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            if (role != null) {
                ids.add(role.getIdLong());
            } else {
                roles.remove(i);
                i -= 1;
            }
        }
        return ids.toArray(new Long[0]);
    }
}