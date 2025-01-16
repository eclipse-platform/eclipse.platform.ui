/*******************************************************************************
 * Copyright (c) 2009, 2025 Oakland Software Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.text.Collator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class TestSorterDataAndResource extends ViewerComparator {

	public boolean _forward = true;

	public TestSorterDataAndResource() {
		super();
	}

	public TestSorterDataAndResource(Collator collator) {
		super(collator);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		String lname = "";
		String rname = "";

		if (e1 instanceof IResource) {
			lname = ((IResource) e1).getName();
		} else if (e1 instanceof TestExtensionTreeData) {
			lname = ((TestExtensionTreeData) e1).getName();
		}

		if (e2 instanceof IResource) {
			rname = ((IResource) e2).getName();
		} else if (e2 instanceof TestExtensionTreeData) {
			rname = ((TestExtensionTreeData) e2).getName();
		}

		if (_forward)
			return lname.compareTo(rname);
		return rname.compareTo(lname);

	}

}
