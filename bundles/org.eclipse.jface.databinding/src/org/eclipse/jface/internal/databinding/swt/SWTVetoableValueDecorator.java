/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 256543, 190881, 263691
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.DecoratingVetoableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 * 
 */
public class SWTVetoableValueDecorator extends DecoratingVetoableValue
		implements ISWTObservableValue {
	private Widget widget;
	private WidgetStringValueProperty property;

	private Listener verifyListener = new Listener() {
		public void handleEvent(Event event) {
			String currentText = (String) property.getValue(widget);
			String newText = currentText.substring(0, event.start) + event.text
					+ currentText.substring(event.end);
			if (!fireValueChanging(Diffs.createValueDiff(currentText, newText))) {
				event.doit = false;
			}
		}
	};

	private Listener disposeListener = new Listener() {
		public void handleEvent(Event event) {
			SWTVetoableValueDecorator.this.dispose();
		}
	};

	/**
	 * @param widget
	 * @param property
	 * @param decorated
	 */
	public SWTVetoableValueDecorator(Widget widget,
			WidgetStringValueProperty property, IObservableValue decorated) {
		super(decorated, true);
		this.property = property;
		this.widget = widget;
		Assert
				.isTrue(decorated.getValueType().equals(String.class),
						"SWTVetoableValueDecorator can only decorate observable values of String value type"); //$NON-NLS-1$
		widget.addListener(SWT.Dispose, disposeListener);
	}

	protected void firstListenerAdded() {
		super.firstListenerAdded();
		widget.addListener(SWT.Verify, verifyListener);
	}

	protected void lastListenerRemoved() {
		if (widget != null && !widget.isDisposed())
			widget.removeListener(SWT.Verify, verifyListener);
		super.lastListenerRemoved();
	}

	public synchronized void dispose() {
		if (widget != null && !widget.isDisposed()) {
			widget.removeListener(SWT.Verify, verifyListener);
		}
		this.widget = null;
		super.dispose();
	}

	public Widget getWidget() {
		return widget;
	}
}
