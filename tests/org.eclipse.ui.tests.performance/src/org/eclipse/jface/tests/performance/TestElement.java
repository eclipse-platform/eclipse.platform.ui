/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

package org.eclipse.jface.tests.performance;

/**
 * The TestElement is the element used for testing
 * viewers.
 */
public class TestElement {

	String name;

	/**
	 * Create a new instance of the receiver.
	 */
	public TestElement() {
		super();
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public TestElement(int index) {
		name = TestTreeElement.generateFirstEntry() + index;
	}

	public String getText() {
		return name;
	}
}
