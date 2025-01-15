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
package org.apache.commons.jxpath;

/**
 * A generic mechanism for accessing collections of name/value pairs.
 * Examples of such collections are HashMap, Properties,
 * ServletContext.  In order to add support for a new such collection
 * type to JXPath, perform the following two steps:
 * <ol>
 * <li>Build an implementation of the DynamicPropertyHandler interface
 * for the desired collection type.</li>
 * <li>Invoke the static method {@link JXPathIntrospector#registerDynamicClass
 * JXPathIntrospector.registerDynamicClass(class, handlerClass)}</li>
 * </ol>
 * JXPath allows access to dynamic properties using these three formats:
 * <ul>
 * <li>{@code "myMap/myKey"}</li>
 * <li>{@code "myMap[@name = 'myKey']"}</li>
 * <li>{@code "myMap[name(.) = 'myKey']"}</li>
 * </ul>
 */
public interface DynamicPropertyHandler {

    /**
     * Returns a list of dynamic property names for the supplied object.
     * @param object to inspect
     * @return String[]
     */
    String[] getPropertyNames(Object object);

    /**
     * Returns the value of the specified dynamic property.
     * @param object to search
     * @param propertyName to retrieve
     * @return Object
     */
    Object getProperty(Object object, String propertyName);

    /**
     * Modifies the value of the specified dynamic property.
     * @param object to modify
     * @param propertyName to modify
     * @param value to set
     */
    void setProperty(Object object, String propertyName, Object value);
}
