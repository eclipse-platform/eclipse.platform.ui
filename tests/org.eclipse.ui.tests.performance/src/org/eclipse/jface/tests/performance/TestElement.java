/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

/**
 * The TestElement is the element used for testing
 * viewers.
 *
 */
public class TestElement {
	
	String name;

	/**
	 * Create a new instance of the receiver.
	 * 
	 */
	public TestElement() {
		super();
	}
	
	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param index
	 */
	public TestElement(int index) {
		name = TestTreeElement.generateFirstEntry() + String.valueOf(index);
	}

	public String getText() {
		return name;
	}
}
