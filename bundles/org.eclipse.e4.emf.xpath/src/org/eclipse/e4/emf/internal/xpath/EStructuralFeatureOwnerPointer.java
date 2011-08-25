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

import java.util.Locale;

import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.e4.emf.internal.xpath.helper.ValueUtils;

/**
 * A pointer describing a node that has properties, each of which could be
 * a collection.
 *
 */
public abstract class EStructuralFeatureOwnerPointer extends NodePointer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Object UNINITIALIZED = new Object();

    private Object value = UNINITIALIZED;

    public NodeIterator childIterator(NodeTest test, boolean reverse,
            NodePointer startWith) {
        if (test == null) {
            return createNodeIterator(null, reverse, startWith);
        }
        if (test instanceof NodeNameTest) {
            NodeNameTest nodeNameTest = (NodeNameTest) test;
            QName testName = nodeNameTest.getNodeName();
            if (isValidProperty(testName)) {
                return createNodeIterator(nodeNameTest.isWildcard() ? null
                        : testName.toString(), reverse, startWith);
            }
            return null;
        }
        return test instanceof NodeTypeTest && ((NodeTypeTest) test).getNodeType() == Compiler.NODE_TYPE_NODE
                ? createNodeIterator(null, reverse, startWith) : null;
    }

    /**
     * Create a NodeIterator.
     * @param property property name
     * @param reverse whether to iterate in reverse
     * @param startWith first pointer to return
     * @return NodeIterator
     */
    public NodeIterator createNodeIterator(String property, boolean reverse,
            NodePointer startWith) {
        return new EStructuralFeatureIterator(this, property, reverse, startWith);
    }

    public NodeIterator attributeIterator(QName name) {
        return new EObjectAttributeIterator(this, name);
    }

    /**
     * Create a new PropertyOwnerPointer.
     * @param parent parent pointer
     * @param locale Locale
     */
    protected EStructuralFeatureOwnerPointer(NodePointer parent, Locale locale) {
        super(parent, locale);
    }

    /**
     * Create a new PropertyOwnerPointer.
     * @param parent pointer
     */
    protected EStructuralFeatureOwnerPointer(NodePointer parent) {
        super(parent);
    }

    public void setIndex(int index) {
        if (this.index != index) {
            super.setIndex(index);
            value = UNINITIALIZED;
        }
    }

    public Object getImmediateNode() {
        if (value == UNINITIALIZED) {
            value = index == WHOLE_COLLECTION ? ValueUtils.getValue(getBaseValue())
                    : ValueUtils.getValue(getBaseValue(), index);
        }
        return value;
    }

    public abstract QName getName();

    /**
     * Learn whether <code>name</code> is a valid child name for this PropertyOwnerPointer.
     * @param name the QName to test
     * @return <code>true</code> if <code>QName</code> is a valid property name.
     * @since JXPath 1.3
     */
    public boolean isValidProperty(QName name) {
        return isDefaultNamespace(name.getPrefix());
    }

    /**
     * Throws an exception if you try to change the root element, otherwise
     * forwards the call to the parent pointer.
     * @param value to set
     */
    public void setValue(Object value) {
        this.value = value;
        if (parent != null) {
            if (parent.isContainer()) {
                parent.setValue(value);
            }
            else {
                if (index == WHOLE_COLLECTION) {
                    throw new UnsupportedOperationException(
                        "Cannot setValue of an object that is not "
                            + "some other object's property");
                }
                throw new JXPathInvalidAccessException(
                    "The specified collection element does not exist: " + this);
            }
        }
        else {
            throw new UnsupportedOperationException(
                "Cannot replace the root object");
        }
    }

    /**
     * If this is a root node pointer, throws an exception; otherwise
     * forwards the call to the parent node.
     */
    public void remove() {
        this.value = null;
        if (parent != null) {
            parent.remove();
        }
        else {
            throw new UnsupportedOperationException(
                "Cannot remove an object that is not "
                    + "some other object's property or a collection element");
        }
    }

    /**
     * Get a PropertyPointer for this PropertyOwnerPointer.
     * @return PropertyPointer
     */
    public abstract EStructuralFeaturePointer getPropertyPointer();

    /**
     * Learn whether dynamic property declaration is supported.
     * @return true if the property owner can set a property "does not exist".
     *         A good example is a Map. You can always assign a value to any
     *         key even if it has never been "declared".
     */
    public boolean isDynamicPropertyDeclarationSupported() {
        return false;
    }

    public int compareChildNodePointers(NodePointer pointer1,
            NodePointer pointer2) {
        int r = pointer1.getName().toString().compareTo(pointer2.getName().toString());
        return r == 0 ? pointer1.getIndex() - pointer2.getIndex() : r;
    }
}
