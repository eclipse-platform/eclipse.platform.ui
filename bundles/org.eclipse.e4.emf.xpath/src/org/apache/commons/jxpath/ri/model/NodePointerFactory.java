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
package org.apache.commons.jxpath.ri.model;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;

/**
 * Creates NodePointers for objects of a certain type.
 * NodePointerFactories are ordered according to the values returned
 * by the "getOrder" method and always queried in that order.
 */
public interface NodePointerFactory {

    /**
     * The factory order number determines its position between other factories.
     * @return int order
     */
    int getOrder();

    /**
     * Create a NodePointer for the supplied object.  The node will represent
     * the "root" object for a path.
     *
     * @param name String node name
     * @param object child object
     * @param locale Locale
     * @return  null if this factory does not recognize objects of the supplied
     * type.
     */
    NodePointer createNodePointer(QName name, Object object, Locale locale);

    /**
     * Create a NodePointer for the supplied child object.
     *
     * @param parent parent node
     * @param name String node name
     * @param object child object
     * @return null if this factory does not recognize objects of the supplied
     * type.
     */
    NodePointer createNodePointer(
        NodePointer parent,
        QName name,
        Object object);
}
