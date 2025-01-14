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
package org.apache.commons.jxpath.ri.model.dynamic;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyIterator;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;

/**
 * @deprecated - no longer needed, as it is identical to PropertyIterator.
 */
@Deprecated
public class DynamicPropertyIterator extends PropertyIterator {

    /**
     * Create a new DynamicPropertyIterator
     * @param pointer PropertyOwnerPointer
     * @param name String
     * @param reverse iteration order
     * @param startWith beginning child
     */
    public DynamicPropertyIterator(
            final PropertyOwnerPointer pointer,
            final String name,
            final boolean reverse,
            final NodePointer startWith) {
        super(pointer, name, reverse, startWith);
    }
}
