/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import org.eclipse.jface.internal.databinding.DataBindingContext;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
 * Provides static methods to create data binding contexts.  Its sole purpose
 * is to provide an API for creating IDataBindingContext implementations.
 * <p>
 * In order to use this class, it is recommended that each project create its
 * own static &lt;projectName&gt;Binding similar to the one in the 
 * org.eclipse.jface.examples.databinding  package in the 
 * org.eclipse.jface.examples.databinding plugin.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class DataBinding {

	/**
	 * Returns a new data binding context on which the given factories have been
	 * registered using
	 * {@link IDataBindingContext#addUpdatableFactory(IUpdatableFactory)}. The
	 * factories will be added in the order given.
	 * 
	 * @param factories
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(
			IUpdatableFactory[] factories) {
		DataBindingContext result = new DataBindingContext();
		if (factories != null)
			for (int i = 0; i < factories.length; i++) {
				result.addUpdatableFactory(factories[i]);
			}
		return result;
	}

	/**
	 * Creates a data binding context whose lifecycle is bound to an SWT
	 * control, using the given factories to create updatable objects from
	 * description objects.
	 * <p>
	 * This method is a convenience method; its implementation creates a new
	 * data binding context by calling
	 * {@link #createContext(IUpdatableFactory[])} and registers a dispose
	 * listener on the given control and calls
	 * {@link IDataBindingContext#dispose()} when the control is disposed of.
	 * </p>
	 * 
	 * @param control
	 * @param factories
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(Control control,
			IUpdatableFactory[] factories) {
		final IDataBindingContext result = createContext(factories);
		control.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				result.dispose();
			}
		});
		return result;
	}

}
