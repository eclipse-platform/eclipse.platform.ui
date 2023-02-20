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

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A Pointer that points to the "lang" attribute of a JavaBean. The value
 * of the attribute is based on the locale supplied to it in the constructor.
 *
 *
 */
public class LangAttributePointer extends NodePointer {

	private static final long serialVersionUID = -8665319197100034134L;

	/**
	 * Create a new LangAttributePointer.
	 * @param parent parent pointer.
	 */
	public LangAttributePointer(NodePointer parent) {
		super(parent);
	}

	@Override
	public QName getName() {
		return new QName("xml", "lang");
	}

	@Override
	public String getNamespaceURI() {
		return null;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public int getLength() {
		return 1;
	}

	@Override
	public Object getBaseValue() {
		return parent.getLocale().toString().replace('_', '-');
	}

	@Override
	public Object getImmediateNode() {
		return getBaseValue();
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Throws UnsupportedOperationException.
	 * @param value Object
	 */
	@Override
	public void setValue(Object value) {
		throw new UnsupportedOperationException(
				"Cannot change locale using the 'lang' attribute");
	}

	@Override
	public String asPath() {
		StringBuilder buffer = new StringBuilder();
		if (parent != null) {
			buffer.append(parent.asPath());
			if (buffer.length() == 0
				|| buffer.charAt(buffer.length() - 1) != '/') {
				buffer.append('/');
			}
		}
		buffer.append("@xml:lang");
		return buffer.toString();
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof LangAttributePointer;
	}

	@Override
	public boolean testNode(NodeTest test) {
		return false;
	}

	@Override
	public int compareChildNodePointers(
		NodePointer pointer1,
		NodePointer pointer2) {
		// Won't happen - lang attributes don't have children
		return 0;
	}
}
