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
/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Iterates property values of an object pointed at with a {@link EStructuralFeatureOwnerPointer}.
 * Examples of such objects are JavaBeans and objects with Dynamic Properties.
 */
public class EStructuralFeatureIterator implements NodeIterator {
    private boolean empty = false;
    private boolean reverse;
    private String name;
    private int startIndex = 0;
    private boolean targetReady = false;
    private int position = 0;
    private EStructuralFeaturePointer propertyNodePointer;
    private int startPropertyIndex;

    private boolean includeStart = false;

    /**
     * Create a new PropertyIterator.
     * @param pointer owning pointer
     * @param name property name
     * @param reverse iteration order
     * @param startWith beginning pointer
     */
    public EStructuralFeatureIterator(
        EStructuralFeatureOwnerPointer pointer,
        String name,
        boolean reverse,
        NodePointer startWith) {
        propertyNodePointer =
            (EStructuralFeaturePointer) pointer.getPropertyPointer().clone();
        this.name = name;
        this.reverse = reverse;
        this.includeStart = true;
        if (reverse) {
            this.startPropertyIndex = EStructuralFeaturePointer.UNSPECIFIED_PROPERTY;
            this.startIndex = -1;
        }
        if (startWith != null) {
            while (startWith != null
                    && startWith.getImmediateParentPointer() != pointer) {
                startWith = startWith.getImmediateParentPointer();
            }
            if (startWith == null) {
                throw new JXPathException(
                    "PropertyIerator startWith parameter is "
                        + "not a child of the supplied parent");
            }
            this.startPropertyIndex =
                ((EStructuralFeaturePointer) startWith).getPropertyIndex();
            this.startIndex = startWith.getIndex();
            if (this.startIndex == NodePointer.WHOLE_COLLECTION) {
                this.startIndex = 0;
            }
            this.includeStart = false;
            if (reverse && startIndex == -1) {
                this.includeStart = true;
            }
        }
    }

    /**
     * Get the property pointer.
     * @return NodePointer
     */
    protected NodePointer getPropertyPointer() {
        return propertyNodePointer;
    }

    /**
     * Reset property iteration.
     */
    public void reset() {
        position = 0;
        targetReady = false;
    }

    public NodePointer getNodePointer() {
        if (position == 0) {
            if (name != null) {
                if (!targetReady) {
                    prepareForIndividualProperty(name);
                }
                // If there is no such property - return null
                if (empty) {
                    return null;
                }
            }
            else {
                if (!setPosition(1)) {
                    return null;
                }
                reset();
            }
        }
        try {
            return propertyNodePointer.getValuePointer();
        }
        catch (Throwable ex) {
            // @todo: should this exception be reported in any way?
            NullEStructuralFeaturePointer npp =
                new NullEStructuralFeaturePointer(
                        propertyNodePointer.getImmediateParentPointer());
            npp.setPropertyName(propertyNodePointer.getPropertyName());
            npp.setIndex(propertyNodePointer.getIndex());
            return npp.getValuePointer();
        }
    }

    public int getPosition() {
        return position;
    }

    public boolean setPosition(int position) {
        return name == null ? setPositionAllProperties(position) : setPositionIndividualProperty(position);
    }

    /**
     * Set position for an individual property.
     * @param position int position
     * @return whether this was a valid position
     */
    private boolean setPositionIndividualProperty(int position) {
        this.position = position;
        if (position < 1) {
            return false;
        }

        if (!targetReady) {
            prepareForIndividualProperty(name);
        }

        if (empty) {
            return false;
        }

        int length = getLength();
        int index;
        if (!reverse) {
            index = position + startIndex;
            if (!includeStart) {
                index++;
            }
            if (index > length) {
                return false;
            }
        }
        else {
            int end = startIndex;
            if (end == -1) {
                end = length - 1;
            }
            index = end - position + 2;
            if (!includeStart) {
                index--;
            }
            if (index < 1) {
                return false;
            }
        }
        propertyNodePointer.setIndex(index - 1);
        return true;
    }

    /**
     * Set position for all properties
     * @param position int position
     * @return whether this was a valid position
     */
    private boolean setPositionAllProperties(int position) {
        this.position = position;
        if (position < 1) {
            return false;
        }

        int offset;
        int count = propertyNodePointer.getPropertyCount();
        if (!reverse) {
            int index = 1;
            for (int i = startPropertyIndex; i < count; i++) {
                propertyNodePointer.setPropertyIndex(i);
                int length = getLength();
                if (i == startPropertyIndex) {
                    length -= startIndex;
                    if (!includeStart) {
                        length--;
                    }
                    offset = startIndex + position - index;
                    if (!includeStart) {
                        offset++;
                    }
                }
                else {
                    offset = position - index;
                }
                if (index <= position && position < index + length) {
                    propertyNodePointer.setIndex(offset);
                    return true;
                }
                index += length;
            }
        }
        else {
            int index = 1;
            int start = startPropertyIndex;
            if (start == EStructuralFeaturePointer.UNSPECIFIED_PROPERTY) {
                start = count - 1;
            }
            for (int i = start; i >= 0; i--) {
                propertyNodePointer.setPropertyIndex(i);
                int length = getLength();
                if (i == startPropertyIndex) {
                    int end = startIndex;
                    if (end == -1) {
                        end = length - 1;
                    }
                    length = end + 1;
                    offset = end - position + 1;
                    if (!includeStart) {
                        offset--;
                        length--;
                    }
                }
                else {
                    offset = length - (position - index) - 1;
                }

                if (index <= position && position < index + length) {
                    propertyNodePointer.setIndex(offset);
                    return true;
                }
                index += length;
            }
        }
        return false;
    }

    /**
     * Prepare for an individual property.
     * @param name property name
     */
    protected void prepareForIndividualProperty(String name) {
        targetReady = true;
        empty = true;

        String[] names = propertyNodePointer.getPropertyNames();
        if (!reverse) {
            if (startPropertyIndex == EStructuralFeaturePointer.UNSPECIFIED_PROPERTY) {
                startPropertyIndex = 0;
            }
            if (startIndex == NodePointer.WHOLE_COLLECTION) {
                startIndex = 0;
            }
            for (int i = startPropertyIndex; i < names.length; i++) {
                if (names[i].equals(name)) {
                    propertyNodePointer.setPropertyIndex(i);
                    if (i != startPropertyIndex) {
                        startIndex = 0;
                        includeStart = true;
                    }
                    empty = false;
                    break;
                }
            }
        }
        else {
            if (startPropertyIndex == EStructuralFeaturePointer.UNSPECIFIED_PROPERTY) {
                startPropertyIndex = names.length - 1;
            }
            if (startIndex == NodePointer.WHOLE_COLLECTION) {
                startIndex = -1;
            }
            for (int i = startPropertyIndex; i >= 0; i--) {
                if (names[i].equals(name)) {
                    propertyNodePointer.setPropertyIndex(i);
                    if (i != startPropertyIndex) {
                        startIndex = -1;
                        includeStart = true;
                    }
                    empty = false;
                    break;
                }
            }
        }
    }

    /**
     * Computes length for the current pointer - ignores any exceptions.
     * @return length
     */
    private int getLength() {
        int length;
        try {
            length = propertyNodePointer.getLength(); // TBD: cache length
        }
        catch (Throwable t) {
            // @todo: should this exception be reported in any way?
            length = 0;
        }
        return length;
    }
}
