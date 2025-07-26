package me.trouper.alias.utils.misc;

import java.util.Map;

public class MapUtils {
    public static <K, V extends Comparable<V>> boolean allValuesMatch(Map<K, V> actual, Map<K, V> required) {
        return required.entrySet().stream().allMatch(entry ->
                actual.containsKey(entry.getKey()) &&
                        actual.get(entry.getKey()).compareTo(entry.getValue()) >= 0
        );
    }
}
