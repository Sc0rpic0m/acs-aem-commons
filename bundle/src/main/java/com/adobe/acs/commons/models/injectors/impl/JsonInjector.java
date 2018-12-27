/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2013 Adobe
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
package com.adobe.acs.commons.models.injectors.impl;


import com.adobe.acs.commons.models.injectors.annotation.Json;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.adobe.acs.commons.models.injectors.impl.InjectorUtils.getResource;
import static com.adobe.acs.commons.util.ReflectionUtil.getClassOrGenericParam;
import static com.adobe.acs.commons.util.ReflectionUtil.getGenericParameter;
import static com.adobe.acs.commons.util.ReflectionUtil.isArray;
import static com.adobe.acs.commons.util.ReflectionUtil.isCollectionType;
import static com.adobe.acs.commons.util.ReflectionUtil.isListType;
import static com.adobe.acs.commons.util.ReflectionUtil.isSetType;
import static com.adobe.acs.commons.util.ReflectionUtil.toArray;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

/**
 * JsonInjector
 * Injects a POJO into a field using GSON.
 * Supports a list or a single value.
 */
@Component(
        property = {
                Constants.SERVICE_RANKING + "=5501"
        },
        service = Injector.class
)
public class JsonInjector implements Injector {

    @Override
    public String getName() {
        return Json.SOURCE;
    }

    private static final Gson GSON = new Gson();

    @Override
    public Object getValue(Object adaptable, String name, Type declaredType, AnnotatedElement element, DisposalCallbackRegistry callbackRegistry) {

        if (element.isAnnotationPresent(Json.class)) {
            Resource resource = getResource(adaptable);
            Json annotation = element.getAnnotation(Json.class);
            String key = defaultIfEmpty(annotation.name(), name);

            if (isSetType(declaredType)) {
                String[] jsonStringArray = resource.getValueMap().get(key, String[].class);
                if (isNotEmpty(jsonStringArray)) {
                    return createSet(jsonStringArray, getGenericParameter(declaredType));
                }
            } else if (isListType(declaredType) || isCollectionType(declaredType)) {
                String[] jsonStringArray = resource.getValueMap().get(key, String[].class);
                if (isNotEmpty(jsonStringArray)) {
                    return createList(jsonStringArray, getGenericParameter(declaredType));
                }
            } else if (isArray(declaredType)) {
                String[] jsonStringArray = resource.getValueMap().get(key, String[].class);
                if (isNotEmpty(jsonStringArray)) {
                    return createArray(jsonStringArray, getGenericParameter(declaredType));
                }
            } else {
                String jsonString = resource.getValueMap().get(key, String.class);
                if (StringUtils.isNotEmpty(jsonString)) {
                    return GSON.fromJson(jsonString, getClassOrGenericParam(declaredType));
                }
            }
        }

        return null;
    }


    private <T> T[] createArray(String[] jsonStringList, Class<T> targetClass) {
        return toArray(createList(jsonStringList, targetClass));
    }

    private <T> Set<T> createSet(String[] jsonStringArray, Class<T> targetClass) {
        return new HashSet<>(createList(jsonStringArray, targetClass));
    }

    private <T> List<T> createList(String[] jsonStringArray, Class<T> targetClass) {

        if (isEmpty(jsonStringArray)) {
            return Collections.emptyList();
        }

        List<String> jsonStringList = Arrays.asList(jsonStringArray);
        return jsonStringList.stream()
                .map(json -> GSON.fromJson(json, targetClass))
                .collect(Collectors.toList());
    }


}