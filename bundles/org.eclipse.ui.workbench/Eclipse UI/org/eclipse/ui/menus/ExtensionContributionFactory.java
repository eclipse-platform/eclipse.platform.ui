/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.menus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * <p>
 * Clients who wish to contribute factories via the
 * <code>org.eclipse.ui.menus</code> extension point should subclass this class
 * rather than the {@link AbstractContributionFactory} as this class provides a
 * default constructor.
 * </p>
 * 
 * <p>
 * Clients must be aware that the results of {@link #getLocation()} and
 * {@link #getNamespace()} will not be valid until
 * {@link #setInitializationData(IConfigurationElement, String, Object)} is
 * invoked. This will occur before
 * {@link #createContributionItems(org.eclipse.ui.services.IServiceLocator, IContributionRoot)}
 * is invoked.
 * </p>
 * 
 * @since 3.5
 */
public abstract class ExtensionContributionFactory extends
		AbstractContributionFactory implements IExecutableExtension {

	private String namespace;
	private String locationURI;

	/**
	 * Create an instance of this class.
	 */
	public ExtensionContributionFactory() {
		super(null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.AbstractContributionFactory#getLocation()
	 */
	public final String getLocation() {
		return locationURI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.AbstractContributionFactory#getNamespace()
	 */
	public final String getNamespace() {
		return namespace;
	}

	/**
	 * Clients who wish to implement their own {@link IExecutableExtension}
	 * behaviour <strong>must</strong> invoke this method prior to any
	 * customization they perform.
	 * 
	 * @throws CoreException
	 *             so that a subclass may throw this
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		locationURI = config
				.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI);
		namespace = config.getNamespaceIdentifier();
	}
}
