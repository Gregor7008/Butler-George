package assets.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public abstract class DataTools {
    

    public static List<Role> getRolesFromArrayKeys(Guild guild, JSONObject data, String primary, String secondary) {
        try {
            JSONArray values = null;
            if (secondary == null) {
                values = data.getJSONArray(primary);
            } else {
                values = data.getJSONObject(primary).getJSONArray(secondary);
            }
            List<Role> roles = new LinkedList<>();
            for (int i = 0; i < values.length(); i++) {
                Role role = guild.getRoleById(values.getLong(i));
                if (role != null) {
                    roles.add(role);
                }
            }
            return roles;
        } catch (JSONException e) {
            return null;
        }
    }
    
    public static <T> void setList(List<T> target, List<T> replacement) {
        if (replacement == null) {
            target.clear();
        } else {
            target = replacement;
            target.removeAll(Collections.singleton(null));
        }
    }
    
    public static <T> void addToList(List<T> target, T[] new_entries) {
        for (int i = 0; i < new_entries.length; i++) {
            if (new_entries[i] != null) {
                target.add(new_entries[i]);
            }
        }
    }
    
    public static <T> void removeFromList(List<T> target, int[] indices) {
        for (int i = 0; i < indices.length; i++) {
            target.remove(indices[i]);
        }
        target.removeAll(Collections.singleton(null));
    }
    
    public static <T> void removeFromList(List<T> target, T[] objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects != null) {
                target.remove(objects[i]);
            }
        }
        target.removeAll(Collections.singleton(null));
    }
    
    public static <K, V> void setMap(ConcurrentHashMap<K, V> target, ConcurrentHashMap<K, V> replacement) {
        if (replacement == null) {
            target.clear();
        } else {
            target = replacement;
            target.values().removeAll(Collections.singleton(null));
            target.keySet().removeAll(Collections.singleton(null));
        }
    }
    
    public static <K, V> void removeFromMap(ConcurrentHashMap<K, V> map, V[] values) {
        List<K> keysToRemove = new ArrayList<>();
        List<V> valueList = new ArrayList<>();
        valueList.addAll(List.of(values));
        map.forEach((key, value) -> {
            if (valueList.contains(value)) {
                keysToRemove.add(key);
            }
        });
        for (int i = 0; i < keysToRemove.size(); i++) {
            map.remove(keysToRemove.get(i));
        }
        map.values().removeAll(Collections.singleton(null));
        map.keySet().removeAll(Collections.singleton(null));
    }
}