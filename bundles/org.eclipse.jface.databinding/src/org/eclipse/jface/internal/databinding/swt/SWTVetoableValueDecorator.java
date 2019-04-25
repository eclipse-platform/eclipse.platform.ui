/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 256543, 190881, 263691, 281723
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.DecoratingVetoableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 *
 */
public class SWTVetoableValueDecorator extends DecoratingVetoableValue<String> implements ISWTObservableValue<String> {
	private Widget widget;
	private WidgetStringValueProperty<Widget> property;

	private Listener verifyListener = event -> {
		String currentText = property.getValue(widget);
		String newText = currentText.substring(0, event.start) + event.text + currentText.substring(event.end);
		if (!fireValueChanging(Diffs.createValueDiff(currentText, newText))) {
			event.doit = false;
		}
	};

	private Listener disposeListener = event -> SWTVetoableValueDecorator.this.dispose();

	/**
	 * @param widget
	 * @param property
	 * @param decorated
	 */
	@SuppressWarnings("unchecked")
	public SWTVetoableValueDecorator(Widget widget,
			WidgetStringValueProperty<? extends Widget> property, IObservableValue<String> decorated) {
		super(decorated, true);
		// This is safe, because the source value will never be written
		this.property = (WidgetStringValueProperty<Widget>) property;
		this.widget = widget;
		Assert.isTrue(decorated.getValueType().equals(String.class),
						"SWTVetoableValueDecorator can only decorate observable values of String value type"); //$NON-NLS-1$
		WidgetListenerUtil.asyncAddListener(widget, SWT.Dispose, disposeListener);
	}

	@Override
	protected void firstListenerAdded() {
		super.firstListenerAdded();
		WidgetListenerUtil.asyncAddListener(widget, SWT.Verify, verifyListener);
	}

	@Override
	protected void lastListenerRemoved() {
		WidgetListenerUtil.asyncRemoveListener(widget, SWT.Verify,
				verifyListener);
		super.lastListenerRemoved();
	}

	@Override
	public synchronized void dispose() {
		WidgetListenerUtil.asyncRemoveListener(widget, SWT.Verify,
				verifyListener);
		WidgetListenerUtil.asyncRemoveListener(widget, SWT.Dispose,
				disposeListener);
		this.widget = null;
		super.dispose();
	}

	@Override
	public Widget getWidget() {
		return widget;
	}
}
