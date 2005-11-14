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

/**
 * 
 * This interface is not intended to be implemented by clients.
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
	 * Policy constant specifying that update or validation should occur automatically. 
	 */
	public static final int POLICY_AUTOMATIC = 1;
	
	/**
	 * Policy constant specifying that update or validation should only occur when explicitly requested. 
	 */
	public static final int POLICY_EXPLICIT = 2;

	/**
	 * Method addBindSupportFactory. Add a factory for converters, validators,
	 * and the like. Adds a factory to the list of factories that will be
	 * consulted when attempting to determine which converter or validator
	 * should be used when binding.
	 * 
	 * @param factory
	 *            the factory to add.
	 */
	public void addBindSupportFactory(IBindSupportFactory factory);

	/**
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
	 * @throws BindingException
	 */
	public void bind(IUpdatable targetUpdatable, IUpdatable modelUpdatable,
			IBindSpec bindSpec) throws BindingException;

	/**
	 * Convenience method to bind targetUpdatable and
	 * createUpdatable(modelDescription).
	 * 
	 * @param targetUpdatable
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind(IUpdatable targetUpdatable, Object modelDescription,
			IBindSpec bindSpec) throws BindingException;

	/**
	 * Convenience method to bind createUpdatable(targetDescription) and
	 * modelUpdatable.
	 * 
	 * @param targetDescription
	 * @param modelUpdatable
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind(Object targetDescription, IUpdatable modelUpdatable,
			IBindSpec bindSpec) throws BindingException;

	/**
	 * Convenience method to bind createUpdatable(targetDescription) and
	 * createUpdatable(modelDescription).
	 * 
	 * @param targetDescription
	 * @param modelDescription
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind(Object targetDescription, Object modelDescription,
			IBindSpec bindSpec) throws BindingException;

	/**
	 * @param description
	 * @return IUpdatable for the given description
	 * @throws BindingException
	 */
	public IUpdatable createUpdatable(Object description)
			throws BindingException;
	
	/**
	 * @param fromType
	 * @param toType
	 * @param modelDescription
	 * @return IValidator
	 */
	public IValidator createValidator(Class fromType, Class toType,
			Object modelDescription);
	
	/**
	 * @param fromType
	 * @param toType
	 * @param modelDescription
	 * @return IConverter
	 */
	public IConverter createConverter(Class fromType, Class toType,
			Object modelDescription);

	/**
	 * 
	 */
	public void dispose();

	/**
	 * @return the validation message updatable value
	 */
	public IUpdatableValue getCombinedValidationMessage();

	/**
	 * @return the validation updatable
	 */
	public IUpdatableValue getPartialValidationMessage();

	/**
	 * @return the validation updatable
	 */
	public IUpdatableValue getValidationMessage();
}