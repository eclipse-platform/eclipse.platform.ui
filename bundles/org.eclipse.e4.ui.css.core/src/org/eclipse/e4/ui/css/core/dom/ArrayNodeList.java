/*******************************************************************************
 * Copyright (c) 2014 Manumitting Technologies Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom;

import java.util.Arrays;
import java.util.List;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ArrayNodeList implements NodeList {

	private Object[] elements;
	private CSSEngine engine;

	public ArrayNodeList(List<?> elements, CSSEngine engine) {
		this(elements.toArray(), engine);
	}

	public ArrayNodeList(Object[] elements, CSSEngine engine) {
		this.elements = elements;
		this.engine = engine;
	}

	@Override
	public int getLength() {
		return elements.length;
	}

	@Override
	public Node item(int index) {
		return engine.getElement(elements[index]);
	}

	@Override
	public String toString() {
		return "ArrayNodeList [elements=" + Arrays.toString(elements) + "]";
	}

}
