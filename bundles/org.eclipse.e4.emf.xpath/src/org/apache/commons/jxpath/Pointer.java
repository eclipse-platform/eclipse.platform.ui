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

import java.io.Serializable;

/**
 * Pointers represent locations of objects and their properties
 * in Java object graphs. JXPathContext has methods
 * ({@link JXPathContext#getPointer(java.lang.String) getPointer()}
 * and  ({@link JXPathContext#iteratePointers(java.lang.String)
 * iteratePointers()}, which, given an XPath, produce Pointers for the objects
 * or properties described the path. For example, {@code ctx.getPointer
 * ("foo/bar")} will produce a Pointer that can get and set the property
 * "bar" of the object which is the value of the property "foo" of the root
 * object. The value of {@code ctx.getPointer("aMap/aKey[3]")} will be a
 * pointer to the 3'rd element of the array, which is the value for the key
 * "aKey" of the map, which is the value of the property "aMap" of the root
 * object.
 */
public interface Pointer extends Cloneable, Comparable, Serializable {

    /**
     * Returns the value of the object, property or collection element
     * this pointer represents. May convert the value to one of the
     * canonical InfoSet types: String, Number, Boolean, Set.
     *
     * For example, in the case of an XML element, getValue() will
     * return the text contained by the element rather than
     * the element itself.
     * @return Object value
     */
    Object getValue();

    /**
     * Returns the raw value of the object, property or collection element
     * this pointer represents.  Never converts the object to a
     * canonical type: returns it as is.
     *
     * For example, for an XML element, getNode() will
     * return the element itself rather than the text it contains.
     * @return Object node
     */
    Object getNode();

    /**
     * Modifies the value of the object, property or collection element
     * this pointer represents.
     * @param value value to set
     */
    void setValue(Object value);

    /**
     * Returns the node this pointer is based on.
     * @return Object
     */
    Object getRootNode();

    /**
     * Returns a string that is a proper "canonical" XPath that corresponds to
     * this pointer.  Consider this example:
     * <p>{@code Pointer  ptr = ctx.getPointer("//employees[firstName = 'John']")
     * }
     * <p>The  value of {@code ptr.asPath()} will look something like
     * {@code "/departments[2]/employees[3]"}, so, basically, it represents
     * the concrete location(s) of the result of a search performed by JXPath.
     * If an object in the pointer's path is a Dynamic Property object (like a
     * Map), the asPath method generates an XPath that looks like this: {@code "
     * /departments[@name = 'HR']/employees[3]"}.
     * @return String path
     */
    String asPath();

    /**
     * Pointers are cloneable.
     * @return cloned Object
     */
    Object clone();
}
