/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 263413, 265561
 *     Ovidio Mallo - bug 270494
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerListProperty;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @since 3.3
 * 
 */
public class SelectionProviderMultipleSelectionProperty extends
		ViewerListProperty {

	private final boolean isPostSelection;

	/**
	 * Constructor.
	 * 
	 * @param isPostSelection
	 *            Whether the post selection or the normal selection is to be
	 *            observed.
	 */
	public SelectionProviderMultipleSelectionProperty(boolean isPostSelection) {
		this.isPostSelection = isPostSelection;
	}

	public Object getElementType() {
		return Object.class;
	}

	protected List doGetList(Object source) {
		ISelection selection = ((ISelectionProvider) source).getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		}
		return Collections.EMPTY_LIST;
	}

	protected void doSetList(Object source, List list, ListDiff diff) {
		doSetList(source, list);
	}

	protected void doSetList(Object source, List list) {
		((ISelectionProvider) source)
				.setSelection(new StructuredSelection(list));
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return new SelectionChangedListener(this, listener, isPostSelection);
	}

	public String toString() {
		return isPostSelection ? "IPostSelectionProvider.postSelection[]" //$NON-NLS-1$
				: "ISelectionProvider.selection[]"; //$NON-NLS-1$
	}
}
