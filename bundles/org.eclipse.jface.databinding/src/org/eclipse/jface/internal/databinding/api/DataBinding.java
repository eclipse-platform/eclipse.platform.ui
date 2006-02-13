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
package org.eclipse.jface.internal.databinding.api;

import org.eclipse.jface.internal.databinding.api.beans.BeanBindSupportFactory;
import org.eclipse.jface.internal.databinding.nonapi.DataBindingContext;

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
	 * @param observableFactories
	 * @param bindSupportFactories 
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(
			IObservableFactory[] observableFactories, IBindSupportFactory[] bindSupportFactories) {
		DataBindingContext result = new DataBindingContext();
		if (observableFactories != null)
			for (int i = 0; i < observableFactories.length; i++) {
				result.addObservableFactory(observableFactories[i]);
			}
		if(bindSupportFactories!=null)
			for (int i = 0; i < bindSupportFactories.length; i++) {
				result.addBindSupportFactory(bindSupportFactories[i]);
			}
		return result;
	}
	
	/**
	 * Returns a new data binding context on which the given factories have been
	 * registered using
	 * {@link IDataBindingContext#addUpdatableFactory(IUpdatableFactory)}. The
	 * factories will be added in the order given.
	 * 
	 * @param observableFactories
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(IObservableFactory[] observableFactories) {
		return createContext(observableFactories, new IBindSupportFactory[] {
				new BeanBindSupportFactory(),
				new DefaultBindSupportFactory()
			});
	}

	/**
	 * Returns a new data binding context with the given parent.
	 * 
	 * @param factories
	 * @return a data binding context
	 */
	public static IDataBindingContext createContext(IDataBindingContext parent) {
		DataBindingContext result = new DataBindingContext((DataBindingContext) parent);
		return result;
	}
	
}
