/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2018 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.util;

import com.adobe.acs.commons.util.impl.ValueMapTypeConverter;
import com.day.cq.commons.inherit.InheritanceValueMap;
import org.apache.sling.api.resource.ValueMap;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Contains reflection utility methods
 */
public class ReflectionUtil {
    private ReflectionUtil() {
        // static methods only
    }

    public static Object convertValueMapValue(ValueMap valueMap, String name, Type declaredType) {
        return new ValueMapTypeConverter(valueMap, name, declaredType).getConvertedValue();
    }

    public static Object convertValueMapValue(InheritanceValueMap valueMap, String name, Type declaredType) {
        return new ValueMapTypeConverter(valueMap, name, declaredType).getConvertedValue();
    }

    public static <T> T[] toArray(Collection<T> c, T[] a) {
        return c.size() > a.length
                ? c.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), c.size()))
                : c.toArray(a);
    }

    /**
     * The collection CAN be empty
     */
    public static <T> T[] toArray(Collection<T> c, Class klass) {
        return toArray(c, (T[]) Array.newInstance(klass, c.size()));
    }

    /**
     * The collection CANNOT be empty!
     */
    public static <T> T[] toArray(Collection<T> c) {
        return toArray(c, c.iterator().next().getClass());
    }

    public static boolean isArray(Type declaredType) {
        if (declaredType instanceof Class<?>) {
            Class<?> clazz = (Class<?>) declaredType;
            return isArray(clazz);
        }
        return false;
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    public static boolean isCollectionType(Type declaredType) {
        if (declaredType instanceof Class<?>) {
            Class<?> clazz = (Class<?>) declaredType;
            return isCollectionType(clazz);
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) declaredType;
            return isCollectionType(parameterizedType.getRawType());
        }
    }

    public static boolean isCollectionType(Class<?> collectionType) {
        return collectionType.equals(Collection.class);
    }

    public static boolean isSetType(Type declaredType) {
        if (declaredType instanceof Class<?>) {
            Class<?> clazz = (Class<?>) declaredType;
            return isSetType(clazz);
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) declaredType;
            return isSetType(parameterizedType.getRawType());
        }
    }

    public static boolean isSetType(Class<?> collectionType) {
        return collectionType.equals(Set.class);
    }

    public static boolean isListType(Type declaredType) {
        if (declaredType instanceof Class<?>) {
            Class<?> clazz = (Class<?>) declaredType;
            return isListType(clazz);
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) declaredType;
            return isListType(parameterizedType.getRawType());
        }
    }

    public static boolean isListType(Class<?> collectionType) {
        return collectionType.equals(List.class);
    }

    public static Class<?> getClassOrGenericParam(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return getGenericParameter(parameterizedType, 0);
        } else {
            Class<?> clazz = (Class<?>) type;
            return clazz;
        }
    }

    public static boolean isAssignableFrom(Type type, Class<?> isAssignableFrom) {

        if (type == null || isAssignableFrom == null) {
            return false;
        }
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            return clazz.isAssignableFrom(isAssignableFrom);
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getRawType().getClass().isAssignableFrom(isAssignableFrom);
        }

    }

    public static boolean hasGenericParameter(Type type) {
        return ParameterizedType.class.isInstance(type);
    }

    public static Class<?> getGenericParameter(Type type) {

        return getGenericParameter(type, 0);
    }

    public static Class<?> getGenericParameter(Type type, int index) {

        if (isArray(type)) {
            return ((Class<?>) type).getComponentType();
        }
        if (hasGenericParameter(type)) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<?>) parameterizedType.getActualTypeArguments()[index];
        }
        return null;
    }
}
