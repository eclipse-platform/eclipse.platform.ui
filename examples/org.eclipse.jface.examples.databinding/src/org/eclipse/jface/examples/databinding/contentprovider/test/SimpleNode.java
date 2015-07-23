/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private String nodeName;
	private IObservableSet<?> children;

	public SimpleNode(String nodeName, IObservableSet<?> children) {
		super();
		this.nodeName = nodeName;
		this.children = children;
	}

	public String getNodeName() {
		return nodeName;
	}

	public IObservableSet<?> getChildren() {
		return children;
	}

}
