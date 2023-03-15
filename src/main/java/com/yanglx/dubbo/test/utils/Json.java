package com.yanglx.dubbo.test.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Json {

    private static final ObjectMapper defaultObjectMapper = newDefaultMapper();
    private static volatile ObjectMapper objectMapper = null;

    public static ObjectMapper newDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
//        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略 map 中的 key
        mapper.configOverride(Map.class)
                .setIgnorals(JsonIgnoreProperties.Value.forIgnoredProperties("class"));
        return mapper;
    }

    /**
     * Get the ObjectMapper used to serialize and deserialize objects to and from JSON values.
     * <p>
     * This can be set to a custom implementation using Json.setObjectMapper.
     *
     * @return the ObjectMapper currently being used
     */
    public static ObjectMapper mapper() {
        if (objectMapper == null) {
            return defaultObjectMapper;
        } else {
            return objectMapper;
        }
    }

    private static String generateJson(Object o, boolean prettyPrint, boolean escapeNonASCII) {
        try {
            ObjectWriter writer = mapper().writer();
            if (prettyPrint) {
                writer = writer.with(SerializationFeature.INDENT_OUTPUT);
            }
            if (escapeNonASCII) {
                writer = writer.with(JsonGenerator.Feature.ESCAPE_NON_ASCII);
            }
            return writer.writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert an object to JsonNode.
     *
     * @param data Value to convert in Json.
     */
    public static JsonNode toJson(final Object data) {
        try {
            return mapper().valueToTree(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T convertValue(Object beanValue, Class<T> clazz) {
        if (beanValue == null) {
            return null;
        }
        return mapper().convertValue(beanValue, clazz);
    }

    public static <T> T convertValue(Object beanValue, TypeReference<T> toValueTypeRef) {
        if (beanValue == null) {
            return null;
        }
        return mapper().convertValue(beanValue, toValueTypeRef);
    }

    public static <T> T convertValue(Object beanValue, Class<?> parametrized, Class<?>... parameterClasses) {
        if (beanValue == null) {
            return null;
        }
        return mapper().convertValue(beanValue, constructType(parametrized, parameterClasses));
    }

    public static <T> List<T> convertListValue(List<?> beanValue, Class<T> parameterClass) {
        return convertValue(beanValue, List.class, parameterClass);
    }

    /**
     * Convert a JsonNode to a Java value
     *
     * @param json  Json value to convert.
     * @param clazz Expected Java value type.
     */
    public static <T> T fromJson(JsonNode json, Class<T> clazz) {
        try {
            return mapper().treeToValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串转换成指定类型的对象
     *
     * @param json  json 字符串
     * @param clazz 指定类型
     * @param <T>   指定类型的泛型参数
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper().readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将Map转换成指定类型的对象
     *
     * @param map   map 对象
     * @param clazz 指定类型
     * @param <T>   指定类型的泛型参数
     * @return 转换后的对象
     */
    public static <T> T fromMap(Map<?, ?> map, Class<T> clazz) {
        return convertValue(map, clazz);
    }

    /**
     * 将输入流转换成指定类型的对象
     *
     * @param json  json 字符串
     * @param clazz 指定类型
     * @param <T>   指定类型的泛型参数
     * @return 转换后的对象
     */
    public static <T> T fromJson(InputStream json, Class<T> clazz) {
        try {
            return mapper().readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 通过 TypeReference 反序列化
     *
     * @param json          json
     * @param typeReference TypeReference 的匿名类或子类
     * @param <T>           需要反序列化的类型
     * @return T 类型的对象
     */
    public static <T> T fromJson(JsonNode json, TypeReference<T> typeReference) {
        return fromJson(stringify(json), typeReference);
    }

    /**
     * 通过 TypeReference 反序列化
     *
     * @param json          json 字符串
     * @param typeReference TypeReference 的匿名类
     * @param <T>           需要反序列化的类型
     * @return T 类型的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return mapper().readValue(json, typeReference);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 通过 TypeReference 反序列化
     *
     * @param json          json 流
     * @param typeReference TypeReference 的匿名类
     * @param <T>           需要反序列化的类型
     * @return T 类型的对象
     */
    public static <T> T fromJson(InputStream json, TypeReference<T> typeReference) {
        try {
            return mapper().readValue(json, typeReference);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 将json转成泛型嵌套类相关的对象
     *
     * @param json             json 对象
     * @param parametrized     集合类型
     * @param parameterClasses 集合类中的泛型类型
     * @param <T>              对象类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(JsonNode json, Class<?> parametrized, Class<?>... parameterClasses) {
        try {
            return mapper().readValue(stringify(json), constructType(parametrized, parameterClasses));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 将字符串转成泛型嵌套类相关的对象
     *
     * @param json             json 字符串
     * @param parametrized     集合类型
     * @param parameterClasses 集合类中的泛型类型
     * @param <T>              对象类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<?> parametrized, Class<?>... parameterClasses) {
        try {
            return mapper().readValue(json, constructType(parametrized, parameterClasses));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 将输入流转成泛型嵌套类相关的对象
     *
     * @param src              src 输入流
     * @param parametrized     泛型类
     * @param parameterClasses 泛型参数类
     * @param <T>              对象类型
     * @return 转换后的对象
     * @since 1.2.2
     */
    public static <T> T fromJson(InputStream src, Class<?> parametrized, Class<?>... parameterClasses) {
        try {
            return mapper().readValue(src, constructType(parametrized, parameterClasses));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 将输入流转成泛型嵌套类相关的对象
     *
     * @param src              URL
     * @param parametrized     泛型类
     * @param parameterClasses 泛型参数类
     * @param <T>              对象类型
     * @return 转换后的对象
     * @since 1.2.2
     */
    public static <T> T fromJson(URL src, Class<?> parametrized, Class<?>... parameterClasses) {
        try {
            return mapper().readValue(src, constructType(parametrized, parameterClasses));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Creates a new empty ObjectNode.
     */
    public static ObjectNode newObject() {
        return mapper().createObjectNode();
    }

    /**
     * Creates a new empty ArrayNode.
     */
    public static ArrayNode newArray() {
        return mapper().createArrayNode();
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    public static String stringify(JsonNode json) {
        return generateJson(json, false, false);
    }

    public static String stringify(Object data) {
        return generateJson(data, false, false);
    }

    /**
     * Convert a JsonNode to its string representation, escaping non-ascii characters.
     */
    public static String asciiStringify(JsonNode json) {
        return generateJson(json, false, true);
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    public static String prettyPrint(JsonNode json) {
        return generateJson(json, true, false);
    }

    /**
     * Convert a JsonNode to its string representation.
     */
    public static String prettyPrint(Object obj) {
        return generateJson(obj, true, false);
    }

    /**
     * Parse a String representing a json, and return it as a JsonNode.
     */
    public static JsonNode parse(String src) {
        try {
            return mapper().readTree(src);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Parse a InputStream representing a json, and return it as a JsonNode.
     */
    public static JsonNode parse(InputStream src) {
        try {
            return mapper().readTree(src);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * Parse a byte array representing a json, and return it as a JsonNode.
     */
    public static JsonNode parse(byte[] src) {
        try {
            return mapper().readTree(src);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static <T> Set<T> convertObjectSet(Set<Object> objectSet, Class<T> clazz, Predicate<T> predicate) {
        if (CollectionUtils.isEmpty(objectSet)) {
            return new HashSet<>();
        }
        return objectSet.stream()
            .map(object -> convertValue(object, clazz))
            .filter(object -> predicate == null || predicate.test(object))
            .collect(Collectors.toSet());
    }

    public static JavaType constructType(Class<?> parametrized, Class<?>... parameterClasses) {
        return mapper().getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    /**
     * Inject the object mapper to use.
     * <p>
     * This is intended to be used when Play starts up.  By default, Play will inject its own object mapper here,
     * but this mapper can be overridden either by a custom module.
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

}
