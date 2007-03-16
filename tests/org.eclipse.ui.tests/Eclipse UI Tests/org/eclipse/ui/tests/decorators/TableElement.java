/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
