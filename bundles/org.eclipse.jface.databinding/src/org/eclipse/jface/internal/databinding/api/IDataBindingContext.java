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

import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;

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
 * @since 3.2
 * 
 */
public interface IDataBindingContext {

	/**
	 * Policy constant specifying that update or validation should occur
	 * automatically whenever a bound observable object generates a change event.
	 */
	public static final int POLICY_AUTOMATIC = 1;

	/**
	 * Policy constant specifying that update or validation should only occur
	 * when explicitly requested by calling {@link #updateModels() } or
	 * {@link #updateTargets() }.
	 */
	public static final int POLICY_EXPLICIT = 2;
	
	/**
	 * Constant specifiying that validation or update events from UI observables
	 * should be triggered early, typically on each keystroke.
	 */
	public static final int TIME_EARLY = 0;
	/**
	 * Constant specifiying that validation or update events from UI observables
	 * should be triggered late, typically on focus lost.
	 */
	public static final int TIME_LATE = 1;	
	

	/**
	 * Adds a factory that can create converters and validators. The list of
	 * bind support factories is used for creating converters and validators
	 * when binding without specifying a converter or validator.
	 * 
	 * @param factory
	 *            the factory to add.
	 */
	public void addBindSupportFactory(IBindSupportFactory factory);

	/**
	 * Adds a factory for creating observable objects from description objects to
	 * this context. The list of observable factories is used for creating
	 * observable objects when binding based on description objects.
	 * 
	 * @param observableFactory
	 */
	public void addObservableFactory(IObservableFactory observableFactory);

	/**
	 * Binds targetObservable and modelObservable using converter and validator as
	 * specified in bindSpec. If bindSpec is null, a default converter and
	 * validator is used.
	 * 
	 * @param targetObservable
	 * @param modelObservable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(IObservable targetObservable, IObservable modelObservable,
			IBindSpec bindSpec) ;

	/**
	 * Convenience method to bind targetObservable and
	 * createObservable(modelDescription).
	 * 
	 * @param targetObservable
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(IObservable targetObservable, Object modelDescription,
			IBindSpec bindSpec);

	/**
	 * Convenience method to bind createObservable(targetDescription) and
	 * modelObservable.
	 * 
	 * @param targetDescription
	 * @param modelObservable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(Object targetDescription, IObservable modelObservable,
			IBindSpec bindSpec) ;

	/**
	 * Convenience method to bind createObservable(targetDescription) and
	 * createObservable(modelDescription).
	 * 
	 * @param targetDescription
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(Object targetDescription, Object modelDescription,
			IBindSpec bindSpec) ;

	/**
	 * Creates an observable object from a description. Description objects are
	 * interpreted by implementors of IObservableFactory, the data binding
	 * framework does not impose any semantics on them.
	 * 
	 * @param description
	 * @return IObservable for the given description
	 */
	public IObservable createObservable(Object description);

	/**
	 * Tries to create a validator that can validate values of type fromType.
	 * Returns <code>null</code> if no validator could be created. Either
	 * toType or modelDescription can be <code>null</code>, but not both. The
	 * implementation of this method will iterate over the registered bind
	 * support factories in reverse order, passing the given arguments to
	 * {@link IBindSupportFactory#createValidator(Class, Class, Object)}. The
	 * first non-null validator will be returned. 
	 * @param fromType
	 * @param toType
	 * @param modelDescription
	 * @return an IValidator, or <code>null</code> if unsuccessful
	 */
	public IValidator createValidator(Object fromType, Object toType);

	/**
	 * Tries to create a converter that can convert from values of type fromType.
	 * Returns <code>null</code> if no converter could be created. Either
	 * toType or modelDescription can be <code>null</code>, but not both. The
	 * implementation of this method will iterate over the registered bind
	 * support factories in reverse order, passing the given arguments to
	 * {@link IBindSupportFactory#createConverter(Object, Object)}. The
	 * first non-null converter will be returned. 
	 * @param fromType
	 * @param toType
	 * @return an IConverter, or <code>null</code> if unsuccessful
	 */
	public IConverter createConverter(Object fromType, Object toType);

	/**
	 * Disposes of this data binding context and all observable objects created
	 * in this context.
	 */
	public void dispose();

	/**
	 * Updates all target observable objects to reflect the current state of the
	 * model observable objects.
	 * 
	 */
	public void updateTargets();

	/**
	 * Updates all model observable objects to reflect the current state of the
	 * target observable objects.
	 * 
	 */
	public void updateModels();

	/**
	 * Returns an observable value of type String
	 * 
	 * @return the validation message observable value
	 */
	public IObservableValue getCombinedValidationMessage();

	/**
	 * Returns an observable value of type String
	 * 
	 * @return the validation observable
	 */
	public IObservableValue getPartialValidationMessage();

	/**
	 * Returns an observable value of type String
	 * 
	 * @return the validation observable
	 */
	public IObservableValue getValidationMessage();

	/**
	 * Add a listener to the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by any
	 * binding created by this data binding context.
	 * 
	 * @param listener The listener to add.
	 */
	public void addBindingEventListener(IBindingListener listener);
	/**
	 * Removes a listener from the set of listeners that will be notified when
	 * an event occurs in the data flow pipeline that is managed by any 
	 * binding created by this data binding context.
	 * 
	 * @param listener The listener to remove.
	 */
	public void removeBindingEventListener(IBindingListener listener);

	/**
	 * Registers an IObservable with the data binding context so that it
	 * will be disposed when all other IObservables are disposed.  This is
	 * only necessary for observables like SettableValue that are instantiated
	 * directly, rather being created by a data binding context to begin
	 * with.
	 * 
	 * @param observable The IObservable to register.
	 */
	public void registerForDispose(IObservable observable);

}

