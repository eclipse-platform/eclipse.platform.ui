/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.ui.internal.ActionExpression;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The DecoratorDefinition is the class that holds onto
 * the label decorator, the name and the name of the
 * class a decorator definition applies to,
 */

public abstract class DecoratorDefinition {

	private String name;
	private String description;
	private ActionExpression enablement;
	private boolean adaptable;
	protected boolean enabled;
	private boolean defaultEnabled;
	private String id;
	protected IConfigurationElement definingElement;

	//A flag that is set if there is an error creating the decorator
	protected boolean labelProviderCreationFailed = false;

	/**
	 * Create a new instance of the receiver with the
	 * supplied values.
	 */

	DecoratorDefinition(
		String identifier,
		String label,
		String decoratorDescription,
		ActionExpression expression,
		boolean isAdaptable,
		boolean initEnabled,
		IConfigurationElement element) {

		this.id = identifier;
		this.name = label;
		this.enablement = expression;
		this.adaptable = isAdaptable;
		this.description = decoratorDescription;
		this.enabled = initEnabled;
		this.defaultEnabled = initEnabled;
		this.definingElement = element;
	}

	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Gets the enabled.
	 * @return Returns a boolean
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled flag and adds or removes the decorator
	 * manager as a listener as appropriate.
	 * @param enabled The enabled to set
	 */
	public void setEnabled(boolean newState) {

		//Only refresh if there has been a change
		if (this.enabled != newState) {
			this.enabled = newState;
			try {
				refreshDecorator();
			} catch (CoreException exception) {
				handleCoreException(exception);
			}

		}
	}

	/**
	 * Refresh the current decorator based on our enable
	 * state.
	 */
	protected abstract void refreshDecorator() throws CoreException;

	/**
	 * Dispose the decorator instance and remove listeners
	 * as appropirate.
	 *  @param decorator
	 */
	protected void disposeCachedDecorator(IBaseLabelProvider disposedDecorator) {
		disposedDecorator.removeListener(
			WorkbenchPlugin.getDefault().getDecoratorManager());
		disposedDecorator.dispose();

	}

	/**
	 * Return whether or not this decorator should be 
	 * applied to adapted types.
	 */

	public boolean isAdaptable() {
		return adaptable;
	}
	/**
	 * Gets the id.
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Return the default value for this type - this value
	 * is the value read from the element description.
	 */
	public boolean getDefaultValue() {
		return defaultEnabled;
	}

	/**
	 * Returns the enablement.
	 * @return ActionExpression
	 */
	public ActionExpression getEnablement() {
		return enablement;
	}

	/**
	 * Add a listener for the decorator.If there is an exception
	 * then inform the user and disable the receiver.
	 * This method should not be called unless a check for
	 * isEnabled() has been done first.
	 */
	void addListener(ILabelProviderListener listener) {
		try {
			//Internal decorator might be null so be prepared
			IBaseLabelProvider currentDecorator = internalGetLabelProvider();
			if (currentDecorator != null)
				currentDecorator.addListener(listener);
		} catch (CoreException exception) {
			handleCoreException(exception);
		}
	}

	/**
	* Return whether or not the decorator registered for element
	* has a label property called property name. If there is an 
	* exception disable the receiver and return false.
	* This method should not be called unless a check for
	* isEnabled() has been done first.
	*/
	boolean isLabelProperty(Object element, String property) {
		try { //Internal decorator might be null so be prepared
			IBaseLabelProvider currentDecorator = internalGetLabelProvider();
			if (currentDecorator != null)
				return currentDecorator.isLabelProperty(element, property);
		} catch (CoreException exception) {
			handleCoreException(exception);
			return false;
		}
		return false;
	}

	/**
	 * Gets the label provider and creates it if it does not exist yet. 
	 * Throws a CoreException if there is a problem
	 * creating the labelProvider.
	 * This method should not be called unless a check for
	 * enabled to be true is done first.
	 * @return Returns a ILabelDecorator
	 */
	protected abstract IBaseLabelProvider internalGetLabelProvider()
		throws CoreException;

	/** 
	* A CoreException has occured. Inform the user and disable
	* the receiver.
	*/

	protected void handleCoreException(CoreException exception) {

		//If there is an error then reset the enabling to false
		InternalPlatform.getRuntimePlugin().getLog().log(exception.getStatus());
		this.enabled = false;
	}

	/**
	 * Return whether or not this is a full or lightweight definition.
	 */
	public abstract boolean isFull();

}
