/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.set.DelegatingSetProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerSetProperty;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.3
 * 
 */
public class ViewerCheckedElementsProperty extends DelegatingSetProperty
		implements IViewerSetProperty {
	ISetProperty checkable;
	ISetProperty checkboxTableViewer;
	ISetProperty checkboxTreeViewer;

	/**
	 * @param elementType
	 */
	public ViewerCheckedElementsProperty(Object elementType) {
		super(elementType);
		checkable = new CheckableCheckedElementsProperty(elementType);
		checkboxTableViewer = new CheckboxTableViewerCheckedElementsProperty(
				elementType);
		checkboxTreeViewer = new CheckboxTreeViewerCheckedElementsProperty(
				elementType);
	}

	protected ISetProperty doGetDelegate(Object source) {
		if (source instanceof CheckboxTableViewer)
			return checkboxTableViewer;
		if (source instanceof CheckboxTreeViewer)
			return checkboxTreeViewer;
		return checkable;
	}

	public IViewerObservableSet observe(Viewer viewer) {
		return (IViewerObservableSet) observe(SWTObservables.getRealm(viewer
				.getControl().getDisplay()), viewer);
	}
}