package assets.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

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
        for (T value : values) {
            if (value != null) {
                target.add(value);
            }
        }
    }
    
    public static <T> void removeIndiciesFromList(List<T> target, int... indices) {
        for (int index : indices) {
            target.remove(index);
        }
    }
    
    @SafeVarargs
    public static <T> void removeValuesFromList(List<T> target, T... values) {
        for (T value : values) {
            if (value != null) {
                target.remove(value);
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
        for (K key : keys) {
            target.remove(key);
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
        for (K key : keysToRemove) {
            target.remove(key);
        }
    }
    
    @SafeVarargs
    public static <T extends MessageConnection> void addDataToMesConMap(ConcurrentHashMap<Long, ConcurrentHashMap<Long, T>> target_map, T... datas) {
        for (T data : datas) {
            long channel_id = data.getChannelId();
            long message_id = data.getMessageId();
            if (channel_id != 0L && message_id != 0L) {
                ConcurrentHashMap<Long, T> stored_map = target_map.get(channel_id);
                if (stored_map != null) {
                    stored_map.put(message_id, data);
                } else {
                    ConcurrentHashMap<Long, T> new_map = new ConcurrentHashMap<>();
                    new_map.put(message_id, data);
                    target_map.put(channel_id, new_map);
                }
            }
        }
    }
    
//  Data conversion tools
	public static List<Long> convertJSONArrayListToLongList(JSONArray array) {
		if (array != null && !array.isEmpty()) {
			List<Long> values_list = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				values_list.add(array.getLong(i));
			}
			return values_list;
		} else {
			return new ArrayList<>();
		}
	}
    
    public static List<Role> convertIdListToRoleList(Guild guild, List<Long> ids) {
        List<Role> roles = new ArrayList<>();
        if (ids != null) {
            for (int i = 0; i < ids.size(); i++) {
                Role role = guild.getRoleById(ids.get(i));
                if (role != null) {
                    roles.add(role);
                } else {
                    ids.remove(i);
                    i -= 1;
                }
            }
        }
        return roles;
    }
    
    public static Long[] convertRoleArrayToIdArray(Role[] roles) {
        ArrayList<Role> roles_list = new ArrayList<>();
        for (Role role : roles) {
            if (role != null) {
                roles_list.add(role);
            }
        }
        return DataTools.convertRoleListToIdArray(roles_list);
    }
    
    public static Long[] convertRoleListToIdArray(List<Role> roles) {
        ArrayList<Long> ids = new ArrayList<>();
        if (roles != null) {
            for (int i = 0; i < roles.size(); i++) {
                Role role = roles.get(i);
                if (role != null) {
                    ids.add(role.getIdLong());
                } else {
                    roles.remove(i);
                    i -= 1;
                }
            }
        }
        return ids.toArray(new Long[0]);
    }
    
    public static <T extends MessageConnection> ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, T>> convertMesConMapToObj(Guild guild, ConcurrentHashMap<Long, ConcurrentHashMap<Long, T>> source_map) {
        ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, T>> return_value = new ConcurrentHashMap<>();
        source_map.forEach((channel_id, map) -> {
            ConcurrentHashMap<Message, T> sub_map = new ConcurrentHashMap<>();
            TextChannel channel = guild.getTextChannelById(channel_id);
            if (channel != null && map != null) {
                map.forEach((message_id, data) -> {
                    Message message = channel.retrieveMessageById(message_id).complete();
                    if (message != null) {
                        sub_map.put(message, data);
                    }
                });
                if (!sub_map.isEmpty()) {
                    return_value.put(channel, sub_map);
                }
            }
        });
        return return_value;
    }
    
    public static <T extends MessageConnection> ConcurrentHashMap<Message, T> convertMesConMapOfChannelToObj(Guild guild, TextChannel channel, ConcurrentHashMap<Long, ConcurrentHashMap<Long, T>> source_map) {
        ConcurrentHashMap<Message, T> return_value = new ConcurrentHashMap<>();
        if (channel != null) {
            ConcurrentHashMap<Long, T> sub_map = source_map.get(channel.getIdLong());
            if (sub_map != null && !sub_map.isEmpty()) {
                sub_map.forEach((message_id, data) -> {
                    Message message = channel.retrieveMessageById(message_id).complete();
                    if (message != null) {
                        return_value.put(message, data);
                    }
                });
            }
        }
        return return_value;
    }
    
    public static <T extends MessageConnection> ConcurrentHashMap<Long, ConcurrentHashMap<Long, T>>  convertMesConMapToIds(ConcurrentHashMap<TextChannel, ConcurrentHashMap<Message, T>> source_map) {
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, T>> converted_map = new ConcurrentHashMap<>();
        if (source_map != null) {
            source_map.forEach((channel, map) -> {
                ConcurrentHashMap<Long, T> sub_map = new ConcurrentHashMap<>();
                if (channel != null && map != null) {
                    map.forEach((message, data) -> {
                        if (message != null) {
                            sub_map.put(message.getIdLong(), data);
                        }
                    });
                    if (!sub_map.isEmpty()) {
                        converted_map.put(channel.getIdLong(), sub_map);
                    }
                }
            });
        }
        return converted_map;
    }
    
    public static <T extends MessageConnection> ConcurrentHashMap<Long, T> convertMesConMapOfChannelToIds(TextChannel channel, ConcurrentHashMap<Message, T> source_map) {
        ConcurrentHashMap<Long, T> converted_map = new ConcurrentHashMap<>();
        if (channel != null && source_map != null) {
            source_map.forEach((message, data) -> { 
                if (message != null) {
                    converted_map.put(message.getIdLong(), data);
                }
            });
        }
        return converted_map;
    }
}