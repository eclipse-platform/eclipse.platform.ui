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

/**
 * A type converter can be installed on {@link TypeUtils} to introduce
 * additional type conversions for JXPath. Most of
 * the time {@link BasicTypeConverter} should be used as the superclass.
 *
 * @see TypeUtils#setTypeConverter
 */
public interface TypeConverter {

    /**
     * Returns true if it can convert the supplied
     * object to the specified class.
     * @param object object to test
     * @param toType target class
     * @return boolean
     */
    boolean canConvert(Object object, Class toType);

    /**
     * Converts the supplied object to the specified
     * type. Throws a runtime exception if the conversion is
     * not possible.
     * @param object object to convert
     * @param toType target class
     * @return resulting Object
     */
    Object convert(Object object, Class toType);
}
