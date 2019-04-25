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
 *     Matthew Hall - initial API and implementation
 *     Matthew Hall - bugs 190881, 264286, 281723
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.swt.ISWTObservableList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <E>
 *            the list element type
 *
 * @since 3.3
 *
 */
public class SWTObservableListDecorator<E> extends DecoratingObservableList<E> implements ISWTObservableList<E> {
	private Widget widget;

	/**
	 * @param decorated
	 * @param widget
	 */
	public SWTObservableListDecorator(IObservableList<E> decorated, Widget widget) {
		super(decorated, true);
		this.widget = widget;
		WidgetListenerUtil.asyncAddListener(widget, SWT.Dispose,
				disposeListener);
	}

	private Listener disposeListener = event -> SWTObservableListDecorator.this.dispose();

	@Override
	public synchronized void dispose() {
		WidgetListenerUtil.asyncRemoveListener(widget, SWT.Dispose,
				disposeListener);
		this.widget = null;
		super.dispose();
	}

	/**
	 * @return Returns the widget.
	 */
	@Override
	public Widget getWidget() {
		return widget;
	}
}
