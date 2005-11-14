/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import org.eclipse.jface.databinding.internal.DataBindingContext;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

/**
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
	 * @param factories
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(IUpdatableFactory[] factories) {
		DataBindingContext result = new DataBindingContext();
		if (factories!=null)
		  for (int i = 0; i < factories.length; i++) {			
				result.addUpdatableFactory(factories[i]);
		  }
		return result;
	}

	/**
	 * Creates and returns a data binding context
	 * 
	 * @param control
	 * @return
	 */
	public static IDataBindingContext createContext(Control control) {
		return createContext(control, new IUpdatableFactory[] {new BeanUpdatableFactory(), new SWTUpdatableFactory(), new ViewersUpdatableFactory()});
	}

	/**	
	 * @param control
	 * @param factories
	 * @return
	 */
	public static IDataBindingContext createContext(Control control, IUpdatableFactory[] factories) {
		final IDataBindingContext result = createContext(factories);
		control.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				result.dispose();
			}
		});
		return result;
	}

}
