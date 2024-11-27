/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.contentprovider.test;

import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * This object will be given randomly-generated children
 *
 * @since 1.0
 */
public class SimpleNode {
	private final String nodeName;
	private final IObservableSet<Object> children;

	public SimpleNode(String nodeName, IObservableSet<Object> children) {
		super();
		this.nodeName = nodeName;
		this.children = children;
	}

	public String getNodeName() {
		return nodeName;
	}

	public IObservableSet<Object> getChildren() {
		return children;
	}

}
