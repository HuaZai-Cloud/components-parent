package cloud.huazai.tool.java.util;

import cloud.huazai.tool.java.constant.StringConstant;
import cloud.huazai.tool.java.lang.ArrayUtils;
import cloud.huazai.tool.java.lang.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {

    public static <T> String toJsonString(Collection<T> collection) {
        return formatCollection(collection);
    }

    public static <T> String toJsonString(T t) {
        return formatValue(t);
    }

    public static <T, E>  String toJsonString(Map<T, E> map) {
        return formatMap(map);
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Map<String, String> fieldMap = parseMap(json);

            for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(instance, parseValue(fieldValue, field.getType(), field.getGenericType()));
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to object", e);
        }
    }


    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        String[] items = splitJsonArray(json);

        for (String item : items) {
            list.add(fromJsonString(item, clazz));
        }

        return list;
    }

    private static Map<String, String> parseMap(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.substring(1, json.length() - 1).trim();

        Pattern pattern = Pattern.compile("\"(.*?)\":\\s*(.*?)(?:,|$)");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();
            map.put(key, value);
        }

        return map;
    }

    private static String[] splitJsonArray(String json) {
        json = json.substring(1, json.length() - 1).trim();
        List<String> items = new ArrayList<>();
        int braceCount = 0;
        int bracketCount = 0;
        StringBuilder sb = new StringBuilder();

        for (char c : json.toCharArray()) {
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (c == '[') bracketCount++;
            if (c == ']') bracketCount--;
            if (c == ',' && braceCount == 0 && bracketCount == 0) {
                items.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            items.add(sb.toString().trim());
        }

        return items.toArray(new String[0]);
    }

    private static <T> T fromJsonString(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            return parseObject(json, clazz);
        } else if (json.startsWith("[") && json.endsWith("]")) {
            return (T) parseArray(json, clazz);
        } else {
            throw new IllegalArgumentException("Invalid JSON string: " + json);
        }
    }

    private static Object parseValue(String value, Class<?> type, Type genericType) {
        if (value == null || value.equals("null")) {
            return null;
        }
        if (type == String.class) {
            return value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length() - 1) : value;
        }
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        }
        if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        }
        if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (type == List.class || type == Collection.class) {
            return parseArray(value, (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0]);
        }
        if (type == Map.class) {
            return parseMap(value);
        }
        // 如果是其他对象类型，递归解析
        return fromJsonString(value, type);
    }



    private static String formatValue(Object value) {
        if (value == null) {
            return StringUtils.NULL;
        }
        if (StringUtils.isString(value)) {
            return StringConstant.DOUBLE_QUOTES+ escapeJsonString((String) value) + StringConstant.DOUBLE_QUOTES;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (CollectionUtils.isCollection(value)) {
            return formatCollection((Collection<?>) value);
        }

        if (MapUtils.isMap(value)) {
            return formatMap((Map<?, ?>) value);
        }
        if (ArrayUtils.isArray(value)) {
            return formatArray(value);
        }
        // 如果是其他对象，递归调用 formatObject
        return formatObject(value);
    }

    private static String formatCollection(Collection<?> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return StringConstant.EMPTY_COLLECTION;
        }
        StringBuilder jsonBuilder = new StringBuilder(StringConstant.LEFT_SQUARE_BRACKET);

        int index = 0;
        for (Object item : collection) {
            if (index > 0) {
                jsonBuilder.append(StringConstant.COMMA+StringConstant.SPACE);
            }
            jsonBuilder.append(formatValue(item));
            index++;
        }
        jsonBuilder.append(StringConstant.RIGHT_SQUARE_BRACKET);
        return jsonBuilder.toString();
    }

    private static String formatMap(Map<?, ?> map) {
        StringBuilder jsonBuilder = new StringBuilder(StringConstant.LEFT_CURLY_BRACE);
        int index = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (index > 0) {
                jsonBuilder.append(StringConstant.COMMA + StringConstant.SPACE);
            }
            jsonBuilder.append(StringConstant.DOUBLE_QUOTES)
                    .append(entry.getKey().toString())
                    .append(StringConstant.DOUBLE_QUOTES + StringConstant.COLON + StringConstant.SPACE)
                    .append(formatValue(entry.getValue()));
            index++;
        }
        jsonBuilder.append(StringConstant.RIGHT_CURLY_BRACE);
        return jsonBuilder.toString();
    }


    private static String formatArray(Object array) {
        StringBuilder jsonBuilder = new StringBuilder(StringConstant.LEFT_SQUARE_BRACKET);
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                jsonBuilder.append(StringConstant.COMMA + StringConstant.SPACE);
            }
            jsonBuilder.append(formatValue(Array.get(array, i)));
        }
        jsonBuilder.append(StringConstant.RIGHT_SQUARE_BRACKET);
        return jsonBuilder.toString();
    }

    private static String formatObject(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder jsonBuilder = new StringBuilder(StringConstant.LEFT_CURLY_BRACE);

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true); // 允许访问私有字段
            try {
                if (i > 0) {
                    jsonBuilder.append(StringConstant.COMMA + StringConstant.SPACE);
                }
                String fieldName = fields[i].getName();
                Object fieldValue = fields[i].get(obj);
                jsonBuilder.append(StringConstant.DOUBLE_QUOTES)
                        .append(fieldName)
                        .append(StringConstant.DOUBLE_QUOTES + StringConstant.COLON + StringConstant.SPACE)
                        .append(formatValue(fieldValue));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field", e);
            }
        }

        jsonBuilder.append(StringConstant.RIGHT_CURLY_BRACE);
        return jsonBuilder.toString();
    }

    private static String escapeJsonString(String str) {
        if (StringUtils.isEmpty(str)) {
            return StringUtils.BLANK;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }


}
