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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Used when there is a need to construct a Pointer for a collection element
 * that does not exist.  For example, if the path is "foo[3]", but the
 * collection "foo" only has one element or is empty or is null, the
 * NullElementPointer can be used to capture this situation without putting a
 * regular NodePointer into an invalid state.  Just create a NullElementPointer
 * with index 2 (= 3 - 1) and a "foo" pointer as the parent.
 *
 * 
 */
public class NullElementPointer extends CollectionPointer {

    private static final long serialVersionUID = 8714236818791036721L;

    /**
     * Create a new NullElementPointer.
     * @param parent parent pointer
     * @param index int
     */
    public NullElementPointer(NodePointer parent, int index) {
        super(parent, (Object) null);
        this.index = index;
    }

    public QName getName() {
        return null;
    }

    public Object getBaseValue() {
        return null;
    }

    public Object getImmediateNode() {
        return null;
    }

    public boolean isLeaf() {
        return true;
    }

    public boolean isCollection() {
        return false;
    }

    /**
     * Get the property pointer for this.
     * @return PropertyPointer
     */
    public EStructuralFeaturePointer getPropertyPointer() {
        return new NullEStructuralFeaturePointer(this);
    }

    public NodePointer getValuePointer() {
        return new NullPointer(this, getName());
    }

    public void setValue(Object value) {
        throw new UnsupportedOperationException(
            "Collection element does not exist: " + this);
    }

    public boolean isActual() {
        return false;
    }

    public boolean isContainer() {
        return true;
    }

    public NodePointer createPath(JXPathContext context) {
        return parent.createChild(context, null, index);
    }

    public NodePointer createPath(JXPathContext context, Object value) {
        return parent.createChild(context, null, index, value);
    }

    public int hashCode() {
        return getImmediateParentPointer().hashCode() + index;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof NullElementPointer)) {
            return false;
        }

        NullElementPointer other = (NullElementPointer) object;
        return getImmediateParentPointer() == other.getImmediateParentPointer()
                && index == other.index;
    }

    public int getLength() {
        return 0;
    }

    public String asPath() {
        StringBuffer buffer = new StringBuffer();
        NodePointer parent = getImmediateParentPointer();
        if (parent != null) {
            buffer.append(parent.asPath());
        }
        if (index != WHOLE_COLLECTION) {
            // Address the list[1][2] case
            if (parent != null && parent.getIndex() != WHOLE_COLLECTION) {
                buffer.append("/.");
            }
            else if (parent != null
                    && parent.getImmediateParentPointer() != null
                    && parent.getImmediateParentPointer().getIndex() != WHOLE_COLLECTION) {
                buffer.append("/.");
            }
            buffer.append("[").append(index + 1).append(']');
        }

        return buffer.toString();
    }
}
