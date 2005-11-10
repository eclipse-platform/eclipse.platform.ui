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
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered early, typically on each keystroke.
	 */
	public static final int TIME_EARLY = 1;

	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should be triggered late, typically on focus lost.
	 */
	public static final int TIME_LATE = 2;

	/**
	 * Constant specifiying that validation or update events from UI updatables
	 * should never be triggered. Note that this means that the updatable will
	 * not track the underlying widget's changes.
	 */
	public static final int TIME_NEVER = 3;

	/**
	 * Key for the update time property in the properties map (see
	 * collectProperties) that is passed to IUpdatableFactory
	 */
	public static final String UPDATE_TIME = "org.eclipse.jface.databinding.updateTime"; //$NON-NLS-1$

	/**
	 * Key for the validation time property in the properties map (see
	 * collectProperties) that is passed to IUpdatableFactory
	 */
	public static final String VALIDATION_TIME = "org.eclipse.jface.databinding.validationTime"; //$NON-NLS-1$

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
	 * Convenience method to bind createUpdatable(new
	 * PropertyDescription(targetObject, targetPropertyID)) and
	 * createUpdatable(new PropertyDescription(modelObject, modelPropertyID))
	 * 
	 * @param targetObject
	 * @param targetPropertyID
	 * @param modelObject
	 * @param modelPropertyID
	 * @param bindSpec
	 *            the bind spec, or null
	 * @throws BindingException
	 */
	public void bind(Object targetObject, Object targetPropertyID,
			Object modelObject, Object modelPropertyID, IBindSpec bindSpec)
			throws BindingException;

	/**
	 * @param description
	 * @return IUpdatable for the given description
	 * @throws BindingException
	 */
	public IUpdatable createUpdatable(Object description)
			throws BindingException;

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
	 * @return the default updateTime
	 */
	public int getUpdateTime();

	/**
	 * @return the validation updatable
	 */
	public IUpdatableValue getValidationMessage();

	/**
	 * @return the default validation time
	 */
	public int getValidationTime();

	/**
	 * @param updateTime
	 */
	public void setUpdateTime(int updateTime);

	/**
	 * @param validationTime
	 */
	public void setValidationTime(int validationTime);
}