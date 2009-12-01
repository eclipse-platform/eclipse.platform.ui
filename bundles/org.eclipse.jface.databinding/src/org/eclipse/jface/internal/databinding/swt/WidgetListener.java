/*******************************************************************************
 * Copyright (c) 2008-2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *         (from WidgetValueProperty.java)
 *     Matthew Hall - bug 294810
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 */
public class WidgetListener extends NativePropertyListener implements Listener {
	private final int[] changeEvents;
	private final int[] staleEvents;

	/**
	 * @param property
	 * @param listener
	 * @param changeEvents
	 * @param staleEvents
	 */
	public WidgetListener(IProperty property, ISimplePropertyListener listener,
			int[] changeEvents, int[] staleEvents) {
		super(property, listener);
		this.changeEvents = changeEvents;
		this.staleEvents = staleEvents;
	}

	public void handleEvent(Event event) {
		if (staleEvents != null)
			for (int i = 0; i < staleEvents.length; i++)
				if (event.type == staleEvents[i]) {
					fireStale(event.widget);
					break;
				}

		if (changeEvents != null)
			for (int i = 0; i < changeEvents.length; i++)
				if (event.type == changeEvents[i]) {
					fireChange(event.widget, null);
					break;
				}
	}

	protected void doAddTo(Object source) {
		Widget widget = (Widget) source;
		if (changeEvents != null) {
			for (int i = 0; i < changeEvents.length; i++) {
				int event = changeEvents[i];
				if (event != SWT.None) {
					WidgetListenerUtil.asyncAddListener(widget, event, this);
				}
			}
		}
		if (staleEvents != null) {
			for (int i = 0; i < staleEvents.length; i++) {
				int event = staleEvents[i];
				if (event != SWT.None) {
					WidgetListenerUtil.asyncAddListener(widget, event, this);
				}
			}
		}
	}

	protected void doRemoveFrom(Object source) {
		Widget widget = (Widget) source;
		if (!widget.isDisposed()) {
			if (changeEvents != null) {
				for (int i = 0; i < changeEvents.length; i++) {
					int event = changeEvents[i];
					if (event != SWT.None)
						WidgetListenerUtil.asyncRemoveListener(widget, event,
								this);
				}
			}
			if (staleEvents != null) {
				for (int i = 0; i < staleEvents.length; i++) {
					int event = staleEvents[i];
					if (event != SWT.None) {
						WidgetListenerUtil.asyncRemoveListener(widget, event,
								this);
					}
				}
			}
		}
	}
}