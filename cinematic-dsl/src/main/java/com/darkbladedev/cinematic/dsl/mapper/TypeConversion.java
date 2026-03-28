package com.darkbladedev.cinematic.dsl.mapper;

import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

public final class TypeConversion {
    private TypeConversion() {
    }

    public static Vector3d toVector3(Object value, String field) {
        if (!(value instanceof List<?> list) || list.size() != 3) {
            throw new SceneMappingException("Se esperaba Vector3 en '" + field + "'");
        }
        return new Vector3d(
                toDouble(list.get(0), field + "[0]"),
                toDouble(list.get(1), field + "[1]"),
                toDouble(list.get(2), field + "[2]")
        );
    }

    public static float toFloat(Object value, String field) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        throw new SceneMappingException("Se esperaba número flotante en '" + field + "'");
    }

    public static double toDouble(Object value, String field) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new SceneMappingException("Se esperaba número en '" + field + "'");
    }

    public static String toStringValue(Object value, String field) {
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new SceneMappingException("Se esperaba texto no vacío en '" + field + "'");
    }

    public static String toOptionalString(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new SceneMappingException("Se esperaba texto opcional en '" + field + "'");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object value, String field) {
        if (value instanceof Map<?, ?> rawMap) {
            return (Map<String, Object>) rawMap;
        }
        throw new SceneMappingException("Se esperaba objeto en '" + field + "'");
    }
}
