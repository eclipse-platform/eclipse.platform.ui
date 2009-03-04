/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 259380, 263413, 265561
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.jface.databinding.viewers.ViewerSetProperty;
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
		return new CheckStateListener(this, listener);
	}

	private class CheckStateListener extends NativePropertyListener implements
			ICheckStateListener {
		private CheckStateListener(IProperty property,
				ISimplePropertyListener listener) {
			super(property, listener);
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
			fireChange(event.getSource(), diff);
		}

		public void doAddTo(Object source) {
			((ICheckable) source).addCheckStateListener(this);
		}

		public void doRemoveFrom(Object source) {
			((ICheckable) source).removeCheckStateListener(this);
		}
	}

	public String toString() {
		String s = "ICheckable.checkedElements{}"; //$NON-NLS-1$
		if (elementType != null)
			s += " <" + elementType + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
