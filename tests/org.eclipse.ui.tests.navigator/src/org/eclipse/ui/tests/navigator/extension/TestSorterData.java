/*******************************************************************************
 * Copyright (c) 2005, 2025 IBM Corporation and others.
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
package org.eclipse.ui.tests.navigator.extension;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class TestSorterData extends ViewerComparator {

	public static String _sorterProperty;
	public static Object _sorterElement;

	public boolean _forward = true;

	public static void resetTest() {
		_sorterProperty = null;
		_sorterElement = null;
	}

	public TestSorterData() {
		super();
	}

	public TestSorterData(Collator collator) {
		super(collator);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		if (e1 instanceof TestExtensionTreeData lvalue) {
			if (e2 instanceof TestExtensionTreeData rvalue) {
				if (_forward)
					return lvalue.getName().compareTo(rvalue.getName());
				return rvalue.getName().compareTo(lvalue.getName());
			}
			return -1;
		} else if(e2 instanceof TestExtensionTreeData) {
			return +1;
		}

		return super.compare(viewer, e1, e2);
	}


	@Override
	public boolean isSorterProperty(Object element, String property) {
		_sorterProperty = property;
		_sorterElement = element;
		return false;
	}


}
