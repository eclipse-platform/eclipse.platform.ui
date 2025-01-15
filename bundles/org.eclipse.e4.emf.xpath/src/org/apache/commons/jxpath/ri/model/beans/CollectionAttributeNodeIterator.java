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
package org.apache.commons.jxpath.ri.model.beans;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Combines attribute node iterators of all elements of a collection into one
 * aggregate attribute node iterator.
 */
public class CollectionAttributeNodeIterator extends CollectionNodeIterator {

    private final QName name;

    /**
     * Create a new CollectionAttributeNodeIterator.
     * @param pointer collection pointer
     * @param name attribute name
     */
    public CollectionAttributeNodeIterator(
        final CollectionPointer pointer,
        final QName name) {
        super(pointer, false, null);
        this.name = name;
    }

    @Override
    protected NodeIterator getElementNodeIterator(final NodePointer elementPointer) {
        return elementPointer.attributeIterator(name);
    }
}
