/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.decorators;

/**
 * The TableElement is the content for tables in the decorator
 * testing.
 */
public class TableElement extends TestElement{

	int index;

	public TableElement(int newIndex) {
		super();
		index = newIndex;
		name = "Table Item " + String.valueOf(index);

	}
}
