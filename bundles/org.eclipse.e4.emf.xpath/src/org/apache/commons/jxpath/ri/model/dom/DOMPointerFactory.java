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
package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.w3c.dom.Node;

/**
 * Implements NodePointerFactory for DOM elements.
 */
public class DOMPointerFactory implements NodePointerFactory {

    /** Factory order */
    public static final int DOM_POINTER_FACTORY_ORDER = 100;

    @Override
    public int getOrder() {
        return DOM_POINTER_FACTORY_ORDER;
    }

    @Override
    public NodePointer createNodePointer(
        final QName name,
        final Object bean,
        final Locale locale) {
        return bean instanceof Node ? new DOMNodePointer((Node) bean, locale) : null;
    }

    @Override
    public NodePointer createNodePointer(
        final NodePointer parent,
        final QName name,
        final Object bean) {
        return bean instanceof Node ? new DOMNodePointer(parent, (Node) bean) : null;
    }
}
