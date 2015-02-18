/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.model;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * An object which can create plug-in related model objects (typically when
 * parsing plug-in manifest files).
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
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

	/**
	 * Returns a new configuration element model which is not initialized.
	 *
	 * @return a new configuration element model
	 */
	public ConfigurationElementModel createConfigurationElement() {
		return new ConfigurationElementModel();
	}

	/**
	 * Returns a new configuration property model which is not initialized.
	 *
	 * @return a new configuration property model
	 */
	public ConfigurationPropertyModel createConfigurationProperty() {
		return new ConfigurationPropertyModel();
	}

	/**
	 * Returns a new extension model which is not initialized.
	 *
	 * @return a new extension model
	 */
	public ExtensionModel createExtension() {
		return new ExtensionModel();
	}

	/**
	 * Returns a new extension point model which is not initialized.
	 *
	 * @return a new extension point model
	 */
	public ExtensionPointModel createExtensionPoint() {
		return new ExtensionPointModel();
	}

	/**
	 * Returns a new library model which is initialized to not export any
	 * of its code.
	 *
	 * @return a new library model
	 */
	public LibraryModel createLibrary() {
		return new LibraryModel();
	}

	/**
	 * Returns a new plug-in descriptor model which is not initialized.
	 *
	 * @return a new plug-in descriptor model
	 */
	public PluginDescriptorModel createPluginDescriptor() {
		return new PluginDescriptorModel();
	}

	/**
	 * Returns a new plug-in fragment model which is not initialized.
	 *
	 * @return a new plug-in fragment model
	 */
	public PluginFragmentModel createPluginFragment() {
		return new PluginFragmentModel();
	}

	/**
	 * Returns a new plug-in prerequisite model which is initialized to
	 * not export its code and to not require an exact match.
	 *
	 * @return a new plug-in prerequisite model
	 */
	public PluginPrerequisiteModel createPluginPrerequisite() {
		return new PluginPrerequisiteModel();
	}

	/**
	 * Returns a new plug-in registry model with an empty plug-in table.
	 *
	 * @return a new plug-in registry model
	 */
	public PluginRegistryModel createPluginRegistry() {
		return new PluginRegistryModel();
	}

	/**
	 * Returns a new URL model which is not initialized.
	 *
	 * @return a new URL model
	 */
	public URLModel createURL() {
		return new URLModel();
	}

	/**
	 * Handles an error state specified by the status.  The collection of all logged status
	 * objects can be accessed using <code>getStatus()</code>.
	 *
	 * @param error a status detailing the error condition
	 */
	public void error(IStatus error) {
		status.add(error);
		if (InternalPlatform.DEBUG)
			System.out.println(error.toString());
	}

	/**
	 * Returns all of the status objects logged thus far by this factory.
	 *
	 * @return a multi-status containing all of the logged status objects
	 */
	public MultiStatus getStatus() {
		return status;
	}
}
