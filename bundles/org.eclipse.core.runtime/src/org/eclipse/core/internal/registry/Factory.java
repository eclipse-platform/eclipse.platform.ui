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
package org.eclipse.core.internal.registry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * An object which can create plug-in related model objects (typically when
 * parsing plug-in manifest files).
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 */
// TODO Why are all the types returned by this factory implementation classes
// and not interfaces?  Can a developer supply a different factory for the registry?
// Not sure if this factory is allowing the registry to be a plugable 
// implementation or not.
public class Factory {
	private MultiStatus status;
	/**
	 * Creates a factory which can be used to create plug-in model objects.
	 * Errors and warnings during parsing etc. can be logged to the given 
	 * status via the <code>error</code> method.
	 *
	 * @param status the status to which errors should be logged
	 */
	public Factory(MultiStatus status) {
		super();
		this.status = status;
	}

	public BundleModel createBundle() {
		return new BundleModel();
	}
	/**
	 * Returns a new configuration element model which is not initialized.
	 *
	 * @return a new configuration element model
	 */
	public ConfigurationElement createConfigurationElement() {
		return new ConfigurationElement();
	}

	/**
	 * Returns a new configuration property model which is not initialized.
	 *
	 * @return a new configuration property model
	 */
	public ConfigurationProperty createConfigurationProperty() {
		return new ConfigurationProperty();
	}

	/**
	 * Returns a new extension model which is not initialized.
	 *
	 * @return a new extension model
	 */
	public Extension createExtension() {
		return new Extension();
	}
	/**
	 * Returns a new extension point model which is not initialized.
	 *
	 * @return a new extension point model
	 */
	public ExtensionPoint createExtensionPoint() {
		return new ExtensionPoint();
	}
	/**
	 * Handles an error state specified by the status.  The collection of all logged status
	 * objects can be accessed using <code>getStatus()</code>.
	 *
	 * @param error a status detailing the error condition
	 */
	public void error(IStatus error) {
		status.add(error);
		System.err.println(error.toString());
	}
	/**
	 * Returns all of the status objects logged thus far by this factory.
	 *
	 * @return a multi-status containing all of the logged status objects
	 */
	public MultiStatus getStatus() {
		return status;
	}
	/**
	 * Returns a new empty extension registry.
	 *
	 * @return a new extension registry
	 */
	public ExtensionRegistry createRegistry() {
		return new ExtensionRegistry(new ExtensionLinker());
	}

}
