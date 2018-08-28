/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Tom Schindl - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * @since 3.3
 *
 */
public class ControlFocusedProperty extends WidgetBooleanValueProperty {
	/**
	 *
	 */
	public ControlFocusedProperty() {
		super();
	}

	@Override
	public boolean doGetBooleanValue(Object source) {
		return ((Control) source).isFocusControl();
	}

	@Override
	public void doSetBooleanValue(Object source, boolean value) {
		if (value)
			((Control) source).setFocus();
	}

	@Override
	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		int[] events = { SWT.FocusIn, SWT.FocusOut };
		return new ControlFocusListener(this, listener, events, null);
	}

	private static class ControlFocusListener extends WidgetListener {
		/**
		 * @param property
		 * @param listener
		 * @param changeEvents
		 * @param staleEvents
		 */
		private ControlFocusListener(IProperty property,
				ISimplePropertyListener listener, int[] changeEvents,
				int[] staleEvents) {
			super(property, listener, changeEvents, staleEvents);
		}

		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.FocusIn:
				fireChange(event.widget, Diffs.createValueDiff(Boolean.FALSE,
						Boolean.TRUE));
				break;
			case SWT.FocusOut:
				fireChange(event.widget, Diffs.createValueDiff(Boolean.TRUE,
						Boolean.FALSE));
				break;
			}
		}
	}

	@Override
	public String toString() {
		return "Control.focus <boolean>"; //$NON-NLS-1$
	}
}
