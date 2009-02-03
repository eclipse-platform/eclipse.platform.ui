/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 263413
 ******************************************************************************/

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.internal.databinding.swt.SWTObservableValueDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract value property implementation for {@link Widget} properties. This
 * class implements some basic behavior that widget properties are generally
 * expected to have, namely:
 * <ul>
 * <li>Calling {@link #observe(Object)} should create the observable on the
 * display realm of the widget, rather than the current default realm
 * <li>All <code>observe()</code> methods should return an
 * {@link ISWTObservableValue}
 * </ul>
 * This class also provides a default widget listener implementation using SWT's
 * {@link Listener untyped listener API}. Subclasses may pass one or more SWT
 * event type constants to the super constructor to indicate which events signal
 * a property change.
 * 
 * @since 1.3
 */
public abstract class WidgetValueProperty extends SimpleValueProperty {
	private int[] events;

	/**
	 * Constructs a WidgetValueProperty which does not listen for any SWT
	 * events.
	 */
	protected WidgetValueProperty() {
		this(null);
	}

	/**
	 * Constructs a WidgetValueProperty with the specified SWT event type
	 * 
	 * @param event
	 *            SWT event type constant of the event that signifies a property
	 *            change.
	 */
	protected WidgetValueProperty(int event) {
		this(new int[] { event });
	}

	/**
	 * Constructs a WidgetValueProperty with the specified SWT event type(s).
	 * 
	 * @param events
	 *            array of SWT event type constants of the events that signify a
	 *            property change.
	 */
	protected WidgetValueProperty(int[] events) {
		this.events = events;
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return events == null ? null : new WidgetListener(listener);
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
