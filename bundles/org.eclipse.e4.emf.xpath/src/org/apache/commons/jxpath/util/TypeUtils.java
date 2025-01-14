/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.util;

import java.util.HashMap;

/**
 * Global type conversion utilities.
 */
public class TypeUtils {
    private static TypeConverter typeConverter = new BasicTypeConverter();
    private static final HashMap PRIMITIVE_TYPE_MAP = new HashMap() {
        private static final long serialVersionUID = 1L;

        {
            put(int.class, Integer.class);
            put(byte.class, Byte.class);
            put(short.class, Short.class);
            put(char.class, Character.class);
            put(long.class, Long.class);
            put(float.class, Float.class);
            put(double.class, Double.class);
            put(boolean.class, Boolean.class);
        }
    };

    /**
     * Install an alternative type converter.
     * @param converter new TypeConverter
     */
    public static synchronized void setTypeConverter(final TypeConverter converter) {
        typeConverter = converter;
    }

    /**
     * Returns the current type converter.
     * @return TypeConverter
     */
    public static TypeConverter getTypeConverter() {
        return typeConverter;
    }

    /**
     * Returns true if the global converter can convert the supplied
     * object to the specified type.
     * @param object object to test
     * @param toType target class
     * @return boolean
     */
    public static boolean canConvert(final Object object, final Class toType) {
        return typeConverter.canConvert(object, toType);
    }

    /**
     * Converts the supplied object to the specified type. May
     * throw a RuntimeException.
     * @param object object to convert
     * @param toType target class
     * @return resulting Object
     */
    public static Object convert(final Object object, final Class toType) {
        return typeConverter.convert(object, toType);
    }

    /**
     * Return the appropriate wrapper type for the specified class.
     * @param p Class for which to retrieve a wrapper class.
     * @return the wrapper if {@code p} is primitive, else {@code p}.
     * @since JXPath 1.3
     */
    public static Class wrapPrimitive(final Class p) {
        return p.isPrimitive() ? (Class) PRIMITIVE_TYPE_MAP.get(p) : p;
    }
}
