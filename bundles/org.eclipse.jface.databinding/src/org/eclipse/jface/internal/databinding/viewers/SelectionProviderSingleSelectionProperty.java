/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222, 263413, 265561
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerValueProperty;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @since 3.3
 * 
 */
public class SelectionProviderSingleSelectionProperty extends
		ViewerValueProperty {
	public Object getValueType() {
		return null;
	}

	protected Object doGetValue(Object source) {
		ISelection selection = ((ISelectionProvider) source).getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).getFirstElement();
		}
		return null;
	}

	protected void doSetValue(Object source, Object value) {
		((ISelectionProvider) source)
				.setSelection(value == null ? StructuredSelection.EMPTY
						: new StructuredSelection(value));
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return new SelectionChangedListener(this, listener);
	}

	public String toString() {
		return "ISelectionProvider.selection"; //$NON-NLS-1$
	}
}
