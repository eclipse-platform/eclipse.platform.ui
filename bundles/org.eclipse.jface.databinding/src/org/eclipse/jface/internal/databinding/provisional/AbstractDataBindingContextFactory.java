/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional;

import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;

/**
 * Defines an interface for objects that can create data binding contexts
 * 
 * @since 3.3
 * @deprecated this class is no longer part of the data binding API
 */
public abstract class AbstractDataBindingContextFactory {
	
	private DataBindingContext context;
	
	/**
	 * Adds the specified IObservableFactory objects to the data binding
	 * context that is being configured in the specified order.
	 * <p>
	 * This method may be overridden in subclasses in order to intercept and
	 * modify the set or order of factories that are configured
	 * for a given data binding context factory.
	 * 
	 * @param observableFactories An IObservableFactory[] containing the factories to add.
	 */
	protected void addObservableFactories(IObservableFactory[] observableFactories) {
		for (int i = 0; i < observableFactories.length; i++) {
			context.addObservableFactory(observableFactories[i]);
		}
	}
	
	/**
	 * Adds the specified BindSupportFactory objects to the data binding
	 * context that is being configured in the specified order.
	 * <p>
	 * This method may be overridden in subclasses in order to intercept and
	 * modify the set or order of factories that are configured
	 * for a given data binding context factory.
	 * 
	 * @param observableFactories An IObservableFactory[] containing the factories to add.
	 */
	protected void addBindSupportFactories(BindSupportFactory[] bindSupportFactories) {
		for (int i = 0; i < bindSupportFactories.length; i++) {
			context.addBindSupportFactory(bindSupportFactories[i]);
		}
	}
	
	/**
	 * Adds the specified IBindingFactory objects to the data binding
	 * context that is being configured in the specified order.
	 * <p>
	 * This method may be overridden in subclasses in order to intercept and
	 * modify the set or order of factories that are configured
	 * for a given data binding context factory.
	 * 
	 * @param observableFactories An IObservableFactory[] containing the factories to add.
	 */
	protected void addBindingFactories(IBindingFactory[] bindingFactories) {
		for (int i = 0; i < bindingFactories.length; i++) {
			context.addBindingFactory(bindingFactories[i]);
		}
	}
	
	/**
	 * Extenders must override this method and call 
	 * {@link #addObservableFactories(IObservableFactory[])},
	 * {@link #addBindSupportFactories(BindSupportFactory[])}, and
	 * {@link #addBindingFactories(IBindingFactory[])} with the appropriate
	 * factories.
	 * @param context TODO
	 */
	protected abstract void configureContext(DataBindingContext context);
	
	/**
	 * Creates, configures, and returns a new data binding context.
	 * 
	 * @return DataBindingContext a configured data binding context.
	 */
	public DataBindingContext createContext() {
		context = new DataBindingContext();
		configureContext(context);
		return context;
	}
}
