/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * AbstractViewerDecorator is the abstract superclass of the decorators used
 * in the viewers tests.
 *
 */
abstract class AbstractViewerDecorator implements ILightweightLabelDecorator {

	/**
	 * Test to see if the element is one of the ones we want to test for.
	 * @param element
	 */
	protected void testForDecorationHit(Object element) {
		if(element.equals(TestTableContentProvider.elements[0]))
			DecoratorViewerTest.tableHit = true;
		if(element.equals(TestTreeContentProvider.root))
			DecoratorViewerTest.treeHit = true;
	}
}
