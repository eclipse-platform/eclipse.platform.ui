/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 259380
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @since 3.3
 * 
 */
public abstract class CheckboxViewerCheckedElementsProperty extends
		ViewerSetProperty {
	private final Object elementType;

	/**
	 * @param elementType
	 */
	public CheckboxViewerCheckedElementsProperty(Object elementType) {
		this.elementType = elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected final Set createElementSet(StructuredViewer viewer) {
		return ViewerElementSet.withComparer(viewer.getComparer());
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return new CheckStateListener(listener);
	}

	public void doAddListener(Object source, INativePropertyListener listener) {
		((ICheckable) source)
				.addCheckStateListener((ICheckStateListener) listener);
	}

	public void doRemoveListener(Object source, INativePropertyListener listener) {
		((ICheckable) source)
				.removeCheckStateListener((ICheckStateListener) listener);
	}

	private class CheckStateListener implements INativePropertyListener,
			ICheckStateListener {
		private ISimplePropertyListener listener;

		private CheckStateListener(ISimplePropertyListener listener) {
			this.listener = listener;
		}

		public void checkStateChanged(CheckStateChangedEvent event) {
			Object element = event.getElement();
			boolean checked = event.getChecked();
			Set elementSet = createElementSet((StructuredViewer) event
					.getCheckable());
			elementSet.add(element);
			Set additions = checked ? elementSet : Collections.EMPTY_SET;
			Set removals = checked ? Collections.EMPTY_SET : elementSet;
			SetDiff diff = Diffs.createSetDiff(additions, removals);
			listener.handlePropertyChange(new SimplePropertyEvent(event
					.getSource(), CheckboxViewerCheckedElementsProperty.this,
					diff));
		}
	}

	public String toString() {
		String s = "ICheckable.checkedElements{}"; //$NON-NLS-1$
		if (elementType != null)
			s += " <" + elementType + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
