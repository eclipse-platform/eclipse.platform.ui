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
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.e4.emf.internal.xpath.helper.ValueUtils;

/**
 * Transparent pointer to a collection (array or Collection).
 *
 */
public class CollectionPointer extends NodePointer {
	private Object collection;
	private NodePointer valuePointer;

	private static final long serialVersionUID = 8620254915563256588L;

	/**
	 * Create a new CollectionPointer.
	 * @param collection value
	 * @param locale Locale
	 */
	public CollectionPointer(Object collection, Locale locale) {
		super(null, locale);
		this.collection = collection;
	}

	/**
	 * Create a new CollectionPointer.
	 * @param parent parent NodePointer
	 * @param collection value
	 */
	public CollectionPointer(NodePointer parent, Object collection) {
		super(parent);
		this.collection = collection;
	}

	@Override
	public QName getName() {
		return null;
	}

	@Override
	public Object getBaseValue() {
		return collection;
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	@Override
	public int getLength() {
		return ValueUtils.getLength(getBaseValue());
	}

	@Override
	public boolean isLeaf() {
		Object value = getNode();
		return value == null || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
	}

	@Override
	public boolean isContainer() {
		return index != WHOLE_COLLECTION;
	}

	@Override
	public Object getImmediateNode() {
		return index == WHOLE_COLLECTION ? ValueUtils.getValue(collection)
				: ValueUtils.getValue(collection, index);
	}

	@Override
	public void setValue(Object value) {
		if (index == WHOLE_COLLECTION) {
			parent.setValue(value);
		}
		else {
			ValueUtils.setValue(collection, index, value);
		}
	}

	@Override
	public void setIndex(int index) {
		super.setIndex(index);
		valuePointer = null;
	}

	@Override
	public NodePointer getValuePointer() {
		if (valuePointer == null) {
			if (index == WHOLE_COLLECTION) {
				valuePointer = this;
			}
			else {
				Object value = getImmediateNode();
				valuePointer =
					NodePointer.newChildNodePointer(this, getName(), value);
			}
		}
		return valuePointer;
	}

	@Override
	public NodePointer createPath(JXPathContext context) {
		if (ValueUtils.getLength(getBaseValue()) <= index) {
			collection = ValueUtils.expandCollection(getNode(), index + 1);
		}
		return this;
	}

	@Override
	public NodePointer createPath(JXPathContext context, Object value) {
		NodePointer ptr = createPath(context);
		ptr.setValue(value);
		return ptr;
	}

	@Override
	public NodePointer createChild(
		JXPathContext context,
		QName name,
		int index,
		Object value) {
		NodePointer ptr = (NodePointer) clone();
		ptr.setIndex(index);
		return ptr.createPath(context, value);
	}

	@Override
	public NodePointer createChild(
		JXPathContext context,
		QName name,
		int index) {
		NodePointer ptr = (NodePointer) clone();
		ptr.setIndex(index);
		return ptr.createPath(context);
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(collection) + index;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof CollectionPointer)) {
			return false;
		}

		CollectionPointer other = (CollectionPointer) object;
		return collection == other.collection && index == other.index;
	}

	@Override
	public NodeIterator childIterator(NodeTest test,
				boolean reverse, NodePointer startWith) {
		if (index == WHOLE_COLLECTION) {
			return new CollectionChildNodeIterator(
				this,
				test,
				reverse,
				startWith);
		}
		return getValuePointer().childIterator(test, reverse, startWith);
	}

	@Override
	public NodeIterator attributeIterator(QName name) {
		return index == WHOLE_COLLECTION ? new CollectionAttributeNodeIterator(this, name)
				: getValuePointer().attributeIterator(name);
	}

	@Override
	public NodeIterator namespaceIterator() {
		return index == WHOLE_COLLECTION ? null : getValuePointer().namespaceIterator();
	}

	@Override
	public NodePointer namespacePointer(String namespace) {
		return index == WHOLE_COLLECTION ? null : getValuePointer().namespacePointer(namespace);
	}

	@Override
	public boolean testNode(NodeTest test) {
		if (index == WHOLE_COLLECTION) {
			if (test == null) {
				return true;
			}
			if (test instanceof NodeNameTest) {
				return false;
			}
			return test instanceof NodeTypeTest && ((NodeTypeTest) test).getNodeType() == Compiler.NODE_TYPE_NODE;
		}
		return getValuePointer().testNode(test);
	}

	@Override
	public int compareChildNodePointers(
				NodePointer pointer1, NodePointer pointer2) {
		return pointer1.getIndex() - pointer2.getIndex();
	}

	@Override
	public String asPath() {
		StringBuilder buffer = new StringBuilder();
		NodePointer parent = getImmediateParentPointer();
		if (parent != null) {
			buffer.append(parent.asPath());
			if (index != WHOLE_COLLECTION) {
				// Address the list[1][2] case
				if (parent.getIndex() != WHOLE_COLLECTION) {
					buffer.append("/.");
				}
				buffer.append("[").append(index + 1).append(']');
			}
		} else if (index != WHOLE_COLLECTION) {
			buffer.append("/.[").append(index + 1).append(']');
		}
		else {
			buffer.append("/");
		}
		return buffer.toString();
	}
}
