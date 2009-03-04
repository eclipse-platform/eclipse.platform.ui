/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 265561)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

class SelectionChangedListener extends NativePropertyListener implements
		ISelectionChangedListener {
	SelectionChangedListener(IProperty property,
			ISimplePropertyListener listener) {
		super(property, listener);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		fireChange(event.getSource(), null);
	}

	public void doAddTo(Object source) {
		((ISelectionProvider) source).addSelectionChangedListener(this);
	}

	public void doRemoveFrom(Object source) {
		((ISelectionProvider) source).removeSelectionChangedListener(this);
	}
}