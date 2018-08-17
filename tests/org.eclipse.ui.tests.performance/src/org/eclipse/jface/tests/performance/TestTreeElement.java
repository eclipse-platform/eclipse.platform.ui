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


public class TestTreeElement extends TestElement {

	TestTreeElement parent;

	TestTreeElement[] children = new TestTreeElement[0];

	private static int index = 0;

	static String characters = "M1NqBwV2CeXrZ3LtKyJ4HuGiF5DoSpA6PaOsI7UdYfT8RgEhW9Qjk0DlWzMxUcsvfbwnm";

	/**
	 * Create a new instance of the receiver .
	 *
	 * @param index
	 * @param treeParent
	 */
	public TestTreeElement(int index, TestTreeElement treeParent) {
		super();
		this.parent = treeParent;
		name = generateFirstEntry() + String.valueOf(index);
	}

	/**
	 * Generate a random string.
	 *
	 * @return String
	 */
	static String generateFirstEntry() {

		String next = characters.substring(index);
		index++;
		if (index > characters.length() - 2)
			index = 0;
		return next;
	}

	/**
	 * Create count number of children in the receiver.
	 *
	 * @param count
	 */
	public void createChildren(int count) {
		children = new TestTreeElement[count];
		for (int i = 0; i < count; i++) {
			children[i] = new TestTreeElement(i, this);
		}
	}

}
