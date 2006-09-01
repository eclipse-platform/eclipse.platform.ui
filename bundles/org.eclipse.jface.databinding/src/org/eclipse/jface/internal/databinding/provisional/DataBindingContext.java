/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.databinding.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IBindingFactory;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;

/**
 * A context for binding observable objects with a shared lifecycle. The
 * factories registered with a data binding context determine how observable
 * objects are created from description objects, and which converters and
 * validators are used when no specific converter or validator is given.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 * @deprecated use {@link org.eclipse.jface.databinding.DataBindingContext} instead
 * 
 */
public final class DataBindingContext extends org.eclipse.jface.databinding.DataBindingContext {

	/**
	 * Returns a new data binding context with the given parent.
	 * 
	 * @param parent
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(DataBindingContext parent) {
		DataBindingContext result = new DataBindingContext(parent);
		return result;
	}

	/**
	 * Returns a new data binding context on which the given factories have been
	 * registered using
	 * {@link DataBindingContext#addObservableFactory(IObservableFactory)}. The
	 * factories will be added in the order given.
	 * 
	 * @param observableFactories
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(
			IObservableFactory[] observableFactories) {
		return createContext(observableFactories,
				new BindSupportFactory[] { new DefaultBindSupportFactory() },
				new IBindingFactory[] { new DefaultBindingFactory() });
	}

	/**
	 * Returns a new data binding context on which the given factories have been
	 * registered using
	 * {@link DataBindingContext#addObservableFactory(IObservableFactory)}. The
	 * factories will be added in the order given.
	 * 
	 * @param observableFactories
	 * @param bindSupportFactories
	 * @param bindingFactories
	 * @return a data binding context
	 */
	public static DataBindingContext createContext(
			IObservableFactory[] observableFactories,
			BindSupportFactory[] bindSupportFactories,
			IBindingFactory[] bindingFactories) {
		DataBindingContext result = new DataBindingContext();
		if (observableFactories != null)
			for (int i = 0; i < observableFactories.length; i++) {
				result.addObservableFactory(observableFactories[i]);
			}
		if (bindSupportFactories != null)
			for (int i = 0; i < bindSupportFactories.length; i++) {
				result.addBindSupportFactory(bindSupportFactories[i]);
			}
		if (bindingFactories != null)
			for (int i = 0; i < bindingFactories.length; i++) {
				result.addBindingFactory(bindingFactories[i]);
			}
		return result;
	}

	private List bindingFactories = new ArrayList();

	private List createdObservables = new ArrayList();

	private List factories = new ArrayList();

	/**
	 * 
	 */
	public DataBindingContext() {
	}

	/**
	 * @param parent
	 * 
	 */
	public DataBindingContext(DataBindingContext parent) {
		super(parent);
	}
	
	/**
	 * Adds a factory for creating observable objects from description objects
	 * to this context. The list of observable factories is used for creating
	 * observable objects when binding based on description objects.
	 * 
	 * @param observableFactory
	 * @deprecated no longer part of the API
	 */
	public void addObservableFactory(IObservableFactory observableFactory) {
		// TODO: consider the fact that adding new factories for a given
		// description
		// may hide default ones (e.g., a new PropertyDescriptor may overide the
		// ond for EMF)
		factories.add(observableFactory);
	}

	/**
	 * Adds the given factory to the list of binding factories.
	 * 
	 * @param factory
	 * @deprecated no longer part of the API
	 */
	public void addBindingFactory(IBindingFactory factory) {
		bindingFactories.add(factory);
	}

	/**
	 * Binds targetObservable and modelObservable using converter and validator
	 * as specified in bindSpec. If bindSpec is null, a default converter and
	 * validator is used.
	 * 
	 * @param targetObservable
	 * @param modelObservable
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 * @deprecated no longer part of the API
	 */
	public Binding bind(IObservable targetObservable,
			IObservable modelObservable, org.eclipse.jface.databinding.BindSpec bindSpec) {
		Binding result = doCreateBinding(targetObservable, modelObservable,
				bindSpec, this);
		if (result != null)
			return result;
		throw new BindingException(
				"No binding found for target: " + targetObservable.getClass().getName() + ", model: " + modelObservable.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Binding doCreateBinding(IObservable targetObservable,
			IObservable modelObservable, org.eclipse.jface.databinding.BindSpec bindSpec,
			DataBindingContext originatingContext) {
		for (int i = bindingFactories.size() - 1; i >= 0; i--) {
			IBindingFactory factory = (IBindingFactory) bindingFactories.get(i);
			Binding binding = null;
			if (bindSpec==null || bindSpec instanceof BindSpec) {
				binding = factory.createBinding(originatingContext, targetObservable,
					modelObservable, (BindSpec)bindSpec);
			}
			if (binding != null) {
				addBinding(binding);
				return binding;
			}
		}
		if (parent instanceof DataBindingContext) {
			return ((DataBindingContext)parent).doCreateBinding(targetObservable, modelObservable,
					bindSpec, originatingContext);
		}
		return null;
	}

	/**
	 * Convenience method to bind targetObservable and
	 * createObservable(modelDescription).
	 * 
	 * @param targetObservable
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 * @deprecated no longer part of the API
	 */
	public Binding bind(IObservable targetObservable, Object modelDescription,
			org.eclipse.jface.databinding.BindSpec bindSpec) {
		return bind(targetObservable, createObservable(modelDescription),
				bindSpec);
	}

	/**
	 * Convenience method to bind createObservable(targetDescription) and
	 * modelObservable.
	 * 
	 * @param targetDescription
	 * @param modelObservable
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 * @deprecated no longer part of the API
	 */
	public Binding bind(Object targetDescription, IObservable modelObservable,
			org.eclipse.jface.databinding.BindSpec bindSpec) {
		return bind(createObservable(targetDescription), modelObservable,
				bindSpec);
	}

	/**
	 * Convenience method to bind createObservable(targetDescription) and
	 * createObservable(modelDescription).
	 * 
	 * @param targetDescription
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null.  Any bindSpec object must not be reused
	 *            or changed after it is passed to #bind.
	 * @return The Binding that manages this data flow
	 * @deprecated no longer part of the API
	 */
	public Binding bind(Object targetDescription, Object modelDescription,
			org.eclipse.jface.databinding.BindSpec bindSpec) {
		return bind(createObservable(targetDescription), modelDescription,
				bindSpec);
	}

	/**
	 * Creates an observable object from a description. Description objects are
	 * interpreted by implementors of IObservableFactory, the data binding
	 * framework does not impose any semantics on them.
	 * 
	 * @param description
	 * @return IObservable for the given description
	 * @deprecated no longer part of the API
	 */
	public IObservable createObservable(Object description) {
		IObservable observable = doCreateObservable(description, this);
		if (observable != null) {
			createdObservables.add(observable);
		}
		return observable;
	}

	/**
	 * Disposes of this data binding context and all observable objects created
	 * in this context.
	 * @deprecated contract has changed in the replacement class
	 */
	public void dispose() {
		super.dispose();
		for (Iterator it = createdObservables.iterator(); it.hasNext();) {
			IObservable observable = (IObservable) it.next();
			observable.dispose();
		}
	}

	private IObservable doCreateObservable(Object description,
			DataBindingContext thisDatabindingContext) {
		for (int i = factories.size() - 1; i >= 0; i--) {
			IObservableFactory factory = (IObservableFactory) factories.get(i);
			IObservable result = factory.createObservable(description);
			if (result != null) {
				return result;
			}
		}
		if (parent instanceof DataBindingContext) {
			return ((DataBindingContext)parent).doCreateObservable(description,
					thisDatabindingContext);
		}
		throw new BindingException("could not find observable for " //$NON-NLS-1$
				+ description);
	}

	/**
	 * Registers an IObservable with the data binding context so that it will be
	 * disposed when all other IObservables are disposed. This is only necessary
	 * for observables like SettableValue that are instantiated directly, rather
	 * being created by a data binding context to begin with.
	 * 
	 * @param observable
	 *            The IObservable to register.
	 * @deprecated no longer part of the API
	 */
	public void registerForDispose(IObservable observable) {
		createdObservables.add(observable);
	}
}