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

import org.eclipse.jface.databinding.converter.IConverter;
import org.eclipse.jface.databinding.validator.IValidator;

/**
 * A context for binding updatable objects with a shared lifecycle. The
 * factories registered with a data binding context determine how updatable
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
	 * automatically whenever a bound updatable object generates a change event.
	 */
	public static final int POLICY_AUTOMATIC = 1;

	/**
	 * Policy constant specifying that update or validation should only occur
	 * when explicitly requested by calling {@link #updateModels() } or
	 * {@link #updateTargets() }.
	 */
	public static final int POLICY_EXPLICIT = 2;
	
	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered early, typically on each keystroke.
	 */
	public static final int TIME_EARLY = 0;
	/**
	 * Constant specifiying that validation or update events from UI updatables
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
	 * Adds a factory for creating updatable objects from description objects to
	 * this context. The list of updatable factories is used for creating
	 * updatable objects when binding based on description objects.
	 * 
	 * @param updatableFactory
	 */
	public void addUpdatableFactory(IUpdatableFactory updatableFactory);

	/**
	 * Binds targetUpdatable and modelUpdatable using converter and validator as
	 * specified in bindSpec. If bindSpec is null, a default converter and
	 * validator is used.
	 * 
	 * @param targetUpdatable
	 * @param modelUpdatable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(IUpdatable targetUpdatable, IUpdatable modelUpdatable,
			IBindSpec bindSpec) ;

	/**
	 * Convenience method to bind targetUpdatable and
	 * createUpdatable(modelDescription).
	 * 
	 * @param targetUpdatable
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(IUpdatable targetUpdatable, Object modelDescription,
			IBindSpec bindSpec);

	/**
	 * Convenience method to bind createUpdatable(targetDescription) and
	 * modelUpdatable.
	 * 
	 * @param targetDescription
	 * @param modelUpdatable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @return The IBinding that manages this data flow
	 */
	public IBinding bind(Object targetDescription, IUpdatable modelUpdatable,
			IBindSpec bindSpec) ;

	/**
	 * Convenience method to bind createUpdatable(targetDescription) and
	 * createUpdatable(modelDescription).
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
	 * Creates an updatable object from a description. Description objects are
	 * interpreted by implementors of IUpdatableFactory, the data binding
	 * framework does not impose any semantics on them.
	 * 
	 * @param description
	 * @return IUpdatable for the given description
	 */
	public IUpdatable createUpdatable(Object description);

	/**
	 * Creates a nested updatable object from a description. Description objects are
	 * interpreted by implementors of IUpdatableFactory, the data binding
	 * framework does not impose any semantics on them.
	 * 
	 * @param nestedProperty
	 * @return IUpdatable for the given description
	 */
	public IUpdatable createNestedUpdatable(NestedProperty nestedProperty);

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
	public IValidator createValidator(Class fromType, Class toType,
			Object modelDescription);

	/**
	 * Tries to create a converter that can convert from values of type fromType.
	 * Returns <code>null</code> if no converter could be created. Either
	 * toType or modelDescription can be <code>null</code>, but not both. The
	 * implementation of this method will iterate over the registered bind
	 * support factories in reverse order, passing the given arguments to
	 * {@link IBindSupportFactory#createConverter(Class, Class, Object)}. The
	 * first non-null converter will be returned. 
	 * @param fromType
	 * @param toType
	 * @param modelDescription
	 * @return an IConverter, or <code>null</code> if unsuccessful
	 */
	public IConverter createConverter(Class fromType, Class toType,
			Object modelDescription);

	/**
	 * Disposes of this data binding context and all updatable objects created
	 * in this context.
	 */
	public void dispose();

	/**
	 * Updates all target updatable objects to reflect the current state of the
	 * model updatable objects.
	 * 
	 */
	public void updateTargets();

	/**
	 * Updates all model updatable objects to reflect the current state of the
	 * target updatable objects.
	 * 
	 */
	public void updateModels();

	/**
	 * Returns an updatable value of type String
	 * 
	 * @return the validation message updatable value
	 */
	public IUpdatableValue getCombinedValidationMessage();

	/**
	 * Returns an updatable value of type String
	 * 
	 * @return the validation updatable
	 */
	public IUpdatableValue getPartialValidationMessage();

	/**
	 * Returns an updatable value of type String
	 * 
	 * @return the validation updatable
	 */
	public IUpdatableValue getValidationMessage();

	/**
	 * Updates the current validation error message originating from the
	 * given listener to an updatable object. 
	 * 
	 * @param listener
	 * @param validationMessage the new message, or <code>null</code>
	 */
	public void updateValidationError(IChangeListener listener,
			String validationMessage);

}