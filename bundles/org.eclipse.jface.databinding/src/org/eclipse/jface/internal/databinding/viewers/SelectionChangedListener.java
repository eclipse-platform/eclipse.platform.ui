/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 265561)
 *     Ovidio Mallo - bug 270494
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

class SelectionChangedListener extends NativePropertyListener implements
		ISelectionChangedListener {

	private final boolean isPostSelection;

	SelectionChangedListener(IProperty property,
			ISimplePropertyListener listener, boolean isPostSelection) {
		super(property, listener);
		this.isPostSelection = isPostSelection;
	}

	public void selectionChanged(SelectionChangedEvent event) {
		fireChange(event.getSource(), null);
	}

	public void doAddTo(Object source) {
		if (isPostSelection) {
			((IPostSelectionProvider) source)
					.addPostSelectionChangedListener(this);
		} else {
			((ISelectionProvider) source).addSelectionChangedListener(this);
		}
	}

	public void doRemoveFrom(Object source) {
		if (isPostSelection) {
			((IPostSelectionProvider) source)
					.removePostSelectionChangedListener(this);
		} else {
			((ISelectionProvider) source).removeSelectionChangedListener(this);
		}
	}
}