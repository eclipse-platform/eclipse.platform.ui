/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

abstract class WidgetValueProperty extends SimpleValueProperty {
	private int[] events;

	WidgetValueProperty() {
		this(null);
	}

	WidgetValueProperty(int event) {
		this(new int[] { event });
	}

	WidgetValueProperty(int[] events) {
		this.events = events;
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return new WidgetListener(listener);
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
		if (events != null) {
			for (int i = 0; i < events.length; i++) {
				int event = events[i];
				if (event != SWT.None) {
					((Widget) source).addListener(event, (Listener) listener);
				}
			}
		}
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
		if (events != null) {
			Widget widget = (Widget) source;
			if (!widget.isDisposed()) {
				for (int i = 0; i < events.length; i++) {
					int event = events[i];
					if (event != SWT.None)
						widget.removeListener(event, (Listener) listener);
				}
			}
		}
	}

	private class WidgetListener implements INativePropertyListener, Listener {
		private final ISimplePropertyListener listener;

		protected WidgetListener(ISimplePropertyListener listener) {
			this.listener = listener;
		}

		public void handleEvent(Event event) {
			listener.handlePropertyChange(new SimplePropertyEvent(event.widget,
					WidgetValueProperty.this, null));
		}
	}

	public IObservableValue observe(Object source) {
		if (source instanceof Widget) {
			return observe(SWTObservables.getRealm(((Widget) source)
					.getDisplay()), source);
		}
		return super.observe(source);
	}

	public IObservableValue observe(Realm realm, Object source) {
		return wrapObservable(super.observe(realm, source), (Widget) source);
	}

	protected ISWTObservableValue wrapObservable(IObservableValue observable,
			Widget widget) {
		return new SWTObservableValueDecorator(observable, widget);
	}
}
