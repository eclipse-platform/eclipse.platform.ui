/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 259380
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.viewers.CheckboxTableViewer;

/**
 * @since 3.3
 * 
 */
public class CheckboxTableViewerCheckedElementsProperty extends
		CheckboxViewerCheckedElementsProperty {
	/**
	 * @param elementType
	 */
	public CheckboxTableViewerCheckedElementsProperty(Object elementType) {
		super(elementType);
	}

	protected Set doGetSet(Object source) {
		CheckboxTableViewer viewer = (CheckboxTableViewer) source;
		Set set = createElementSet(viewer);
		set.addAll(Arrays.asList(viewer.getCheckedElements()));
		return set;
	}

	protected void doSetSet(Object source, Set set, SetDiff diff) {
		CheckboxTableViewer viewer = (CheckboxTableViewer) source;
		viewer.setCheckedElements(set.toArray());
	}

	public String toString() {
		String s = "CheckboxTableViewer.checkedElements{}"; //$NON-NLS-1$
		if (getElementType() != null)
			s += " <" + getElementType() + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
