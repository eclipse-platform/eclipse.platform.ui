/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.swt;

import org.eclipse.jface.databinding.observable.value.AbstractObservableValue;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

/**
 * An abstract superclass for observable values that gurantees that the 
 * observable will be disposed when the control to which it is attached is
 * disposed.
 * 
 * @since 3.3
 */
public abstract class AbstractSWTObservableValue extends AbstractObservableValue {

	/**
	 * Standard constructor for an SWT ObservableValue.  Makes sure that
	 * the observable gets disposed when the SWT widget is disposed.
	 * 
	 * @param widget
	 */
	protected AbstractSWTObservableValue(Widget widget) {
		widget.addDisposeListener(disposeListener);
	}
	
	private DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			AbstractSWTObservableValue.this.dispose();
		}
	};
}
