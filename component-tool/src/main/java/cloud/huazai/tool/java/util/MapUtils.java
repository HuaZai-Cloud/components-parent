package cloud.huazai.tool.java.util;

import cloud.huazai.tool.java.lang.ObjectUtils;
import cloud.huazai.tool.java.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MapUtils
 *
 * @author devon
 * @since 2024/12/11
 */

public class MapUtils {

    private static final String defaultMessage = "Collection Is Empty";

    public static Map<?,?> immutableEmptyMap() {
        return Collections.emptyMap();
    }

    public static Map<?,?> emptyMap() {
        return new HashMap<>();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isMap(Object obj) {
        return ObjectUtils.isNotNull(obj) && obj instanceof Map;
    }

    public static void requireNonEmpty(Map<?, ?> map, String message) {
        if (StringUtils.isBlank(message)) {
            message = defaultMessage;
        }
        if (isEmpty(map)) {
            throw new NullPointerException(message);
        }
    }
}
